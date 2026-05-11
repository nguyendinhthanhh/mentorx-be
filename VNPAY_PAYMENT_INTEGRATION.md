# VNPay Payment Integration - Complete Guide

## 📋 Overview

VNPay payment gateway has been successfully integrated into the MentorX backend for handling deposit transactions. Users can now deposit money into their wallet using VNPay.

## 🏗️ Architecture

### Components Created

1. **Configuration**
   - `VNPayConfig.java` - VNPay configuration properties

2. **DTOs**
   - `VNPayPaymentRequest.java` - Request to create payment
   - `VNPayPaymentResponse.java` - Payment URL response
   - `VNPayCallbackResponse.java` - Callback/return response

3. **Service Layer**
   - `VNPayService.java` - Service interface
   - `VNPayServiceImpl.java` - Service implementation with payment logic

4. **Utility**
   - `VNPayUtil.java` - Helper methods for HMAC SHA512, hash generation, etc.

5. **Controller**
   - `PaymentController.java` - REST API endpoints

## 🔧 Configuration

### Environment Variables (.env)

```env
VNPAY_TMN_CODE=LIT9JOW0
VNPAY_HASH_SECRET=7FMGS92TYFEIWVX085T1AIJ4RLO98AFY
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:3000/payment/vnpay-return
```

### Application Configuration (application.yml)

```yaml
vnpay:
  tmn-code: ${VNPAY_TMN_CODE:LIT9JOW0}
  hash-secret: ${VNPAY_HASH_SECRET:7FMGS92TYFEIWVX085T1AIJ4RLO98AFY}
  url: ${VNPAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
  return-url: ${VNPAY_RETURN_URL:http://localhost:3000/payment/vnpay-return}
  version: 2.1.0
  command: pay
  order-type: other
```

## 🚀 API Endpoints

### 1. Create Payment URL

**Endpoint:** `POST /api/v1/payment/vnpay/create`

**Request Body:**
```json
{
  "amount": 100000,
  "orderInfo": "Nap tien vao vi MentorX",
  "bankCode": "NCB"
}
```

**Parameters:**
- `amount` (required): Amount in VND (minimum 10,000 VND)
- `orderInfo` (optional): Description of the order
- `bankCode` (optional): Bank code (NCB, VNPAYQR, VNBANK, INTCARD, etc.)

**Response:**
```json
{
  "code": 200,
  "message": "Payment URL created successfully",
  "data": {
    "code": "00",
    "message": "Success",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
  }
}
```

**Usage:**
```bash
curl -X POST http://localhost:8080/api/v1/payment/vnpay/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "amount": 100000,
    "orderInfo": "Nap tien vao vi",
    "bankCode": "NCB"
  }'
```

### 2. Payment Callback/Return

**Endpoint:** `GET /api/v1/payment/vnpay/callback`

This endpoint is called by VNPay after payment completion. It processes the payment result and updates the wallet balance.

**Query Parameters:** (Automatically sent by VNPay)
- `vnp_Amount`: Payment amount
- `vnp_BankCode`: Bank code
- `vnp_ResponseCode`: Response code (00 = success)
- `vnp_TransactionNo`: VNPay transaction number
- `vnp_TxnRef`: Order ID
- `vnp_SecureHash`: Security hash
- ... and other VNPay parameters

**Response:**
```json
{
  "code": 200,
  "message": "Callback processed",
  "data": {
    "code": "00",
    "message": "Payment successful",
    "orderId": "12345678",
    "amount": 100000,
    "transactionNo": "14012345",
    "bankCode": "NCB",
    "payDate": "20260511153000"
  }
}
```

### 3. Payment Return (Frontend Redirect)

**Endpoint:** `GET /api/v1/payment/vnpay/return`

This is the same as callback but designed for frontend to handle the redirect from VNPay.

## 💰 Payment Flow

### 1. User Initiates Payment

```
User → Frontend → POST /api/v1/payment/vnpay/create
```

- User enters amount to deposit
- Frontend calls create payment API
- Backend creates `DepositOrder` with status `PENDING`
- Backend generates VNPay payment URL
- Frontend redirects user to VNPay payment page

### 2. User Completes Payment

```
User → VNPay Payment Page → Completes Payment
```

- User selects bank and completes payment on VNPay
- VNPay processes the payment

### 3. VNPay Callback

```
VNPay → Backend → GET /api/v1/payment/vnpay/callback
```

- VNPay sends callback to backend with payment result
- Backend verifies signature
- If payment successful (code = "00"):
  - Update `DepositOrder` status to `COMPLETED`
  - Add amount to user's `Wallet`
  - Create `WalletTransaction` record
- If payment failed:
  - Update `DepositOrder` status to `FAILED`

### 4. User Redirected Back

```
VNPay → Frontend → GET /api/v1/payment/vnpay/return
```

- VNPay redirects user back to frontend
- Frontend calls return endpoint to get payment result
- Frontend shows success/failure message to user

## 💾 Database Changes

### DepositOrder Table

When payment is created:
```sql
INSERT INTO deposit_orders (
  user_id, gateway, gateway_order_id, 
  real_amount, real_currency, mxc_amount, 
  exchange_rate, txn_status
) VALUES (
  'user-uuid', 'VNPAY', '12345678',
  100000, 'VND', 10.0000,
  0.0001, 'PENDING'
);
```

When payment is completed:
```sql
UPDATE deposit_orders SET
  txn_status = 'COMPLETED',
  gateway_txn_id = '14012345',
  gateway_response = '{"vnp_Amount":"10000000",...}',
  reconciled_at = NOW()
WHERE gateway_order_id = '12345678';
```

### Wallet Table

```sql
UPDATE wallets SET
  balance_mxc = balance_mxc + 10.0000
WHERE user_id = 'user-uuid';
```

### WalletTransaction Table

```sql
INSERT INTO wallet_transactions (
  wallet_id, transaction_group_id, txn_type,
  direction, amount_mxc, balance_after_mxc,
  reference_id, reference_type, note,
  txn_status, entry_hash
) VALUES (
  'wallet-uuid', 'group-uuid', 'DEPOSIT',
  'IN', 10.0000, 110.0000,
  'deposit-order-uuid', 'DEPOSIT_ORDER', 
  'Deposit via VNPay - Order: 12345678',
  'COMPLETED', 'hash-value'
);
```

## 🔐 Security

### Signature Verification

All VNPay callbacks are verified using HMAC SHA512:

```java
String hashData = VNPayUtil.hashAllFields(params);
String calculatedHash = VNPayUtil.hmacSHA512(hashSecret, hashData);
boolean isValid = calculatedHash.equalsIgnoreCase(receivedHash);
```

### Transaction Integrity

- Each transaction has a unique `entry_hash` for ledger integrity
- `DepositOrder` has unique constraint on `(gateway, gateway_order_id)`
- Prevents duplicate processing of the same payment

## 💱 Exchange Rate

Current exchange rate: **1 VND = 0.0001 MXC**

Example:
- Deposit 100,000 VND → Receive 10 MXC
- Deposit 1,000,000 VND → Receive 100 MXC

You can modify the exchange rate in `VNPayServiceImpl.java`:

```java
private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.0001");
```

## 🧪 Testing

### Test with VNPay Sandbox

1. **Create Payment:**
```bash
curl -X POST http://localhost:8080/api/v1/payment/vnpay/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount": 100000}'
```

2. **Get Payment URL from response**

3. **Open URL in browser**

4. **Use VNPay test cards:**
   - Card Number: `9704198526191432198`
   - Card Holder: `NGUYEN VAN A`
   - Expiry Date: `07/15`
   - OTP: `123456`

5. **Complete payment**

6. **Check callback response**

### Verify Database

```sql
-- Check deposit order
SELECT * FROM deposit_orders 
WHERE gateway_order_id = 'YOUR_ORDER_ID';

-- Check wallet balance
SELECT * FROM wallets 
WHERE user_id = 'YOUR_USER_ID';

-- Check transaction
SELECT * FROM wallet_transactions 
WHERE reference_type = 'DEPOSIT_ORDER'
ORDER BY created_at DESC LIMIT 1;
```

## 📝 VNPay Response Codes

| Code | Description |
|------|-------------|
| 00 | Transaction successful |
| 07 | Transaction successful, suspected fraud |
| 09 | Card not registered for Internet Banking |
| 10 | Card authentication failed |
| 11 | Payment timeout |
| 12 | Card locked |
| 13 | Wrong OTP |
| 24 | Transaction cancelled |
| 51 | Insufficient balance |
| 65 | Transaction limit exceeded |
| 75 | Payment bank under maintenance |
| 79 | Payment timeout, please retry |
| 99 | Unknown error |

## 🔄 Frontend Integration Example

```typescript
// Create payment
const createPayment = async (amount: number) => {
  const response = await fetch('/api/v1/payment/vnpay/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ amount })
  });
  
  const data = await response.json();
  
  // Redirect to VNPay
  window.location.href = data.data.paymentUrl;
};

// Handle return from VNPay
const handlePaymentReturn = async () => {
  const params = new URLSearchParams(window.location.search);
  const queryParams = Object.fromEntries(params);
  
  const response = await fetch(
    `/api/v1/payment/vnpay/return?${params.toString()}`
  );
  
  const data = await response.json();
  
  if (data.data.code === '00') {
    // Payment successful
    showSuccess('Payment successful!');
  } else {
    // Payment failed
    showError('Payment failed!');
  }
};
```

## 🚨 Important Notes

1. **Sandbox vs Production:**
   - Current config uses VNPay Sandbox
   - For production, change `VNPAY_URL` to: `https://vnpayment.vn/paymentv2/vpcpay.html`
   - Get production credentials from VNPay

2. **Return URL:**
   - Must be accessible from internet for VNPay callback
   - For local testing, use ngrok or similar tunneling service
   - Update `VNPAY_RETURN_URL` accordingly

3. **Security:**
   - Never expose `VNPAY_HASH_SECRET` in frontend
   - Always verify signature on backend
   - Use HTTPS in production

4. **Transaction Timeout:**
   - Payment expires after 15 minutes
   - User must complete payment within this time

## 📚 References

- [VNPay Documentation](https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/)
- [VNPay Sandbox](https://sandbox.vnpayment.vn/)

## ✅ Checklist

- [x] VNPay configuration
- [x] Payment creation endpoint
- [x] Payment callback handling
- [x] Signature verification
- [x] Wallet balance update
- [x] Transaction recording
- [x] Error handling
- [x] Logging
- [x] Documentation

## 🎉 Ready to Use!

The VNPay payment integration is complete and ready for testing. Start the backend server and try creating a payment!

# VNPay Payment - Quick Start Guide

## ✅ Đã hoàn thành

VNPay payment gateway đã được tích hợp thành công vào backend MentorX!

## 🚀 Cách sử dụng

### 1. Khởi động Backend

```bash
cd mentorx-be
./mvnw spring-boot:run
```

### 2. Test API với Postman/cURL

#### Tạo Payment URL

```bash
POST http://localhost:8080/api/v1/payment/vnpay/create
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "amount": 100000,
  "orderInfo": "Nap tien vao vi MentorX",
  "bankCode": "NCB"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment URL created successfully",
  "data": {
    "code": "00",
    "message": "Success",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
  },
  "timestamp": "2026-05-11 15:30:00"
}
```

#### Mở Payment URL

Copy `paymentUrl` từ response và mở trong browser để thanh toán.

#### Test Card VNPay Sandbox

- **Số thẻ:** 9704198526191432198
- **Tên chủ thẻ:** NGUYEN VAN A
- **Ngày hết hạn:** 07/15
- **Mã OTP:** 123456

### 3. Xem kết quả

Sau khi thanh toán thành công, VNPay sẽ gọi callback và:
- Cập nhật `deposit_orders` với status `COMPLETED`
- Cộng tiền vào `wallets`
- Tạo record trong `wallet_transactions`

## 📁 Files đã tạo

### Configuration
- `VNPayConfig.java` - VNPay configuration

### DTOs
- `VNPayPaymentRequest.java` - Request tạo payment
- `VNPayPaymentResponse.java` - Response payment URL
- `VNPayCallbackResponse.java` - Response callback

### Service
- `VNPayService.java` - Interface
- `VNPayServiceImpl.java` - Implementation

### Utility
- `VNPayUtil.java` - Helper methods

### Controller
- `PaymentController.java` - REST endpoints

### Config Files
- `application.yml` - Thêm VNPay config
- `.env` - Thêm VNPay credentials

## 🔧 Cấu hình

### VNPay Credentials (Sandbox)

```env
VNPAY_TMN_CODE=LIT9JOW0
VNPAY_HASH_SECRET=7FMGS92TYFEIWVX085T1AIJ4RLO98AFY
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:3000/payment/vnpay-return
```

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payment/vnpay/create` | Tạo payment URL |
| GET | `/api/v1/payment/vnpay/callback` | VNPay callback (IPN) |
| GET | `/api/v1/payment/vnpay/return` | User return URL |

## 💱 Exchange Rate

**1 VND = 0.0001 MXC**

Ví dụ:
- Nạp 100,000 VND → Nhận 10 MXC
- Nạp 1,000,000 VND → Nhận 100 MXC

## 📝 Response Codes

| Code | Ý nghĩa |
|------|---------|
| 00 | Giao dịch thành công |
| 07 | Trừ tiền thành công, nghi ngờ gian lận |
| 09 | Thẻ chưa đăng ký Internet Banking |
| 10 | Xác thực thẻ thất bại |
| 11 | Hết thời gian thanh toán |
| 24 | Giao dịch bị hủy |
| 51 | Tài khoản không đủ số dư |
| 99 | Lỗi không xác định |

## 🔍 Kiểm tra Database

```sql
-- Xem deposit order
SELECT * FROM deposit_orders 
WHERE gateway = 'VNPAY' 
ORDER BY created_at DESC LIMIT 5;

-- Xem wallet balance
SELECT u.email, w.balance_mxc 
FROM wallets w 
JOIN users u ON w.user_id = u.id;

-- Xem transactions
SELECT * FROM wallet_transactions 
WHERE txn_type = 'DEPOSIT' 
ORDER BY created_at DESC LIMIT 5;
```

## 📚 Documentation

Xem chi tiết tại: [VNPAY_PAYMENT_INTEGRATION.md](./VNPAY_PAYMENT_INTEGRATION.md)

## ⚠️ Lưu ý

1. **Sandbox vs Production:**
   - Hiện tại đang dùng Sandbox
   - Để chuyển sang Production, đổi `VNPAY_URL` thành: `https://vnpayment.vn/paymentv2/vpcpay.html`

2. **Return URL:**
   - Cần accessible từ internet để VNPay callback
   - Với local testing, dùng ngrok hoặc tương tự

3. **Security:**
   - Không expose `VNPAY_HASH_SECRET` ra frontend
   - Luôn verify signature ở backend
   - Dùng HTTPS trong production

## ✨ Next Steps

1. **Frontend Integration:**
   - Tạo UI để user nhập số tiền
   - Redirect đến VNPay payment URL
   - Handle return từ VNPay
   - Hiển thị kết quả thanh toán

2. **Testing:**
   - Test với nhiều số tiền khác nhau
   - Test các trường hợp lỗi
   - Test timeout scenario

3. **Production:**
   - Đăng ký VNPay production account
   - Cập nhật credentials
   - Deploy và test trên production

## 🎉 Hoàn thành!

VNPay payment đã sẵn sàng để sử dụng. Hãy test và báo lỗi nếu có vấn đề!

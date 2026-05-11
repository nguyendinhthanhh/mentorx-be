# VNPay Frontend Integration - Complete Guide

## ✅ Đã hoàn thành

VNPay payment đã được tích hợp hoàn chỉnh vào frontend MentorX tại trang `/wallet`!

## 🎨 UI Components đã tạo

### 1. **DepositForm Component** (Updated)
**Location:** `src/components/wallet/DepositForm.tsx`

**Features:**
- ✅ Quick amount selection (50K, 100K, 200K, 500K, 1M, 2M VND)
- ✅ Custom amount input với validation
- ✅ Real-time VND → MXC conversion display
- ✅ Bank selection (All Banks, VNPay QR, Local Bank, International Card)
- ✅ Beautiful gradient UI với icons
- ✅ Loading state khi redirect
- ✅ Error handling

**UI Preview:**
```
┌─────────────────────────────────────┐
│ 💱 Exchange Rate                    │
│ 1 VND = 0.0001 MXC • Min: 10,000   │
├─────────────────────────────────────┤
│ Quick Select Amount                 │
│ [50K] [100K] [200K]                │
│ [500K] [1M] [2M]                   │
├─────────────────────────────────────┤
│ Custom Amount (VND)                 │
│ 💰 [100,000] VND                   │
├─────────────────────────────────────┤
│ You will receive                    │
│ 💳 10.0000 MXC                     │
├─────────────────────────────────────┤
│ Payment Method                      │
│ [🏦 All] [📱 QR] [🏛️ Bank] [💳 Card]│
├─────────────────────────────────────┤
│ [💳 Pay with VNPay]                │
└─────────────────────────────────────┘
```

### 2. **VNPayReturnPage** (New)
**Location:** `src/pages/payment/VNPayReturnPage.tsx`

**Features:**
- ✅ Xử lý callback từ VNPay
- ✅ Verify payment với backend
- ✅ Hiển thị success/error state
- ✅ Payment details (Order ID, Transaction No, Amount)
- ✅ Navigation buttons (Go to Wallet, Back to Dashboard)
- ✅ Beautiful loading animation
- ✅ Error message mapping

**Success Screen:**
```
┌─────────────────────────────────────┐
│         ✅ Payment Successful!       │
│   Your wallet has been credited     │
├─────────────────────────────────────┤
│ Order ID: 12345678                  │
│ Transaction No: 14012345            │
│ Amount: 100,000 VND                 │
│ MXC Received: 10.0000 MXC          │
├─────────────────────────────────────┤
│ [Go to Wallet →]                   │
│ [Back to Dashboard]                 │
└─────────────────────────────────────┘
```

**Error Screen:**
```
┌─────────────────────────────────────┐
│         ❌ Payment Failed            │
│   Insufficient balance              │
├─────────────────────────────────────┤
│ Order ID: 12345678                  │
│ Amount: 100,000 VND                 │
├─────────────────────────────────────┤
│ [Try Again]                         │
│ [Back to Dashboard]                 │
└─────────────────────────────────────┘
```

### 3. **Payment API** (New)
**Location:** `src/api/paymentApi.ts`

**Methods:**
- `createVNPayPayment(data)` - Tạo payment URL
- `processVNPayCallback(params)` - Xử lý callback

## 🔄 Payment Flow

### User Journey:

1. **User vào Wallet Page** (`/wallet`)
   - Xem balance hiện tại
   - Click tab "Deposit"

2. **Chọn số tiền nạp**
   - Click quick amount hoặc nhập custom
   - Chọn bank (optional)
   - Click "Pay with VNPay"

3. **Redirect đến VNPay**
   - Frontend gọi API `POST /api/v1/payment/vnpay/create`
   - Backend trả về payment URL
   - Browser redirect đến VNPay

4. **Thanh toán trên VNPay**
   - User nhập thông tin thẻ
   - Xác thực OTP
   - VNPay xử lý payment

5. **VNPay redirect về**
   - VNPay redirect về `/payment/vnpay-return?vnp_...`
   - Frontend gọi API `GET /api/v1/payment/vnpay/return`
   - Backend verify và update wallet
   - Hiển thị kết quả

6. **User quay lại Wallet**
   - Click "Go to Wallet"
   - Xem balance đã được cập nhật

## 📁 Files đã tạo/cập nhật

### New Files:
1. `src/api/paymentApi.ts` - Payment API client
2. `src/pages/payment/VNPayReturnPage.tsx` - Return page
3. `VNPAY_FRONTEND_INTEGRATION.md` - Documentation

### Updated Files:
1. `src/components/wallet/DepositForm.tsx` - Tích hợp VNPay
2. `src/App.tsx` - Thêm route `/payment/vnpay-return`

## 🚀 Cách test

### 1. Start Frontend

```bash
cd mentorx-fe
npm run dev
```

### 2. Start Backend

```bash
cd mentorx-be
./mvnw spring-boot:run
```

### 3. Test Flow

1. **Login:** http://localhost:3000/login
2. **Go to Wallet:** http://localhost:3000/wallet
3. **Click Deposit tab**
4. **Select amount:** Click "100K" hoặc nhập custom
5. **Click "Pay with VNPay"**
6. **Trên VNPay sandbox:**
   - Số thẻ: `9704198526191432198`
   - Tên: `NGUYEN VAN A`
   - Ngày hết hạn: `07/15`
   - OTP: `123456`
7. **Complete payment**
8. **Verify:**
   - Xem success message
   - Click "Go to Wallet"
   - Check balance đã tăng

## 🎨 UI Features

### Design Highlights:

1. **Modern Gradient UI**
   - Primary gradient colors
   - Smooth transitions
   - Shadow effects

2. **Interactive Elements**
   - Quick amount buttons
   - Bank selection cards
   - Hover effects

3. **Real-time Feedback**
   - Live VND → MXC conversion
   - Loading states
   - Error messages

4. **Responsive Design**
   - Mobile-friendly
   - Grid layouts
   - Flexible components

### Color Scheme:

- **Primary:** Blue gradient (primary-600 to primary-700)
- **Success:** Green (green-600)
- **Error:** Red (red-600)
- **Info:** Blue (blue-50)
- **Neutral:** Gray scale

## 💡 Code Examples

### Create Payment

```typescript
import { paymentApi } from '@/api/paymentApi'

const handlePayment = async () => {
  try {
    const response = await paymentApi.createVNPayPayment({
      amount: 100000,
      orderInfo: 'Nap tien vao vi',
      bankCode: 'NCB' // optional
    })
    
    // Redirect to VNPay
    window.location.href = response.paymentUrl
  } catch (error) {
    console.error('Payment error:', error)
  }
}
```

### Process Callback

```typescript
import { paymentApi } from '@/api/paymentApi'

const processCallback = async (searchParams: URLSearchParams) => {
  const params: Record<string, string> = {}
  searchParams.forEach((value, key) => {
    params[key] = value
  })
  
  const result = await paymentApi.processVNPayCallback(params)
  
  if (result.code === '00') {
    console.log('Payment successful!')
  } else {
    console.log('Payment failed:', result.message)
  }
}
```

## 🔧 Configuration

### Backend URL

Update trong `src/api/client.ts` nếu cần:

```typescript
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  // ...
})
```

### VNPay Return URL

Backend config trong `application.yml`:

```yaml
vnpay:
  return-url: http://localhost:3000/payment/vnpay-return
```

## 🐛 Troubleshooting

### Issue: Payment URL không tạo được

**Solution:**
- Check backend đang chạy
- Check JWT token còn valid
- Check console logs

### Issue: Callback không hoạt động

**Solution:**
- Check return URL đúng format
- Check backend logs
- Verify VNPay credentials

### Issue: Balance không cập nhật

**Solution:**
- Check database transactions
- Check wallet_transactions table
- Refresh wallet page

## 📊 Error Codes

| Code | Message | Action |
|------|---------|--------|
| 00 | Success | Show success |
| 07 | Suspected fraud | Contact support |
| 09 | Card not registered | Use different card |
| 10 | Auth failed | Retry |
| 11 | Timeout | Retry |
| 24 | Cancelled | Retry |
| 51 | Insufficient balance | Add funds |
| 99 | Unknown error | Contact support |

## ✨ Features Summary

### ✅ Completed:

- [x] VNPay payment integration
- [x] Beautiful deposit form UI
- [x] Quick amount selection
- [x] Bank selection
- [x] Real-time conversion display
- [x] Payment return page
- [x] Success/error handling
- [x] Loading states
- [x] Error messages
- [x] Navigation flows
- [x] Responsive design
- [x] API integration
- [x] Route configuration

### 🎯 User Experience:

- **Fast:** Quick amount buttons
- **Clear:** Real-time conversion
- **Secure:** VNPay integration
- **Beautiful:** Modern gradient UI
- **Responsive:** Mobile-friendly
- **Informative:** Clear feedback

## 🎉 Ready to Use!

VNPay payment đã được tích hợp hoàn chỉnh vào frontend. User có thể:

1. ✅ Nạp tiền vào wallet
2. ✅ Chọn số tiền nhanh
3. ✅ Thanh toán qua VNPay
4. ✅ Xem kết quả thanh toán
5. ✅ Quay lại wallet với balance mới

**Test ngay tại:** http://localhost:3000/wallet

Enjoy! 🚀

package com.mentorx.api.feature.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.payment.config.VNPayConfig;
import com.mentorx.api.feature.payment.dto.request.VNPayPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.VNPayCallbackResponse;
import com.mentorx.api.feature.payment.dto.response.VNPayPaymentResponse;
import com.mentorx.api.feature.payment.service.VNPayService;
import com.mentorx.api.feature.payment.util.VNPayUtil;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.entity.WalletTransaction;
import com.mentorx.api.feature.wallet.repository.DepositOrderRepository;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import com.mentorx.api.feature.wallet.repository.WalletTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final DepositOrderRepository depositOrderRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Exchange rate: 1 VND = 0.0001 MXC (example rate)
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.0001");

    @Override
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            // Get current user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Generate order ID
            String orderId = VNPayUtil.getRandomNumber(8);
            
            // Calculate MXC amount
            BigDecimal mxcAmount = request.getAmount().multiply(EXCHANGE_RATE).setScale(4, RoundingMode.HALF_UP);

            // Create deposit order
            DepositOrder depositOrder = DepositOrder.builder()
                    .user(user)
                    .gateway(PaymentGateway.VNPAY)
                    .gatewayOrderId(orderId)
                    .realAmount(request.getAmount())
                    .realCurrency("VND")
                    .mxcAmount(mxcAmount)
                    .exchangeRate(EXCHANGE_RATE)
                    .txnStatus(TxnStatus.PENDING)
                    .build();
            depositOrderRepository.save(depositOrder);

            // Build VNPay parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(request.getAmount().multiply(new BigDecimal("100")).longValue()));
            vnpParams.put("vnp_CurrCode", "VND");
            
            if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
                vnpParams.put("vnp_BankCode", request.getBankCode());
            }
            
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", request.getOrderInfo() != null ? request.getOrderInfo() : "Nap tien vao vi MentorX");
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpRequest));

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Build query string and hash
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data and query using the same encoded values
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()).replace("+", "%20");
                    
                    if (hashData.length() > 0) {
                        hashData.append('&');
                        query.append('&');
                    }
                    
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(encodedValue);
                    
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(encodedValue);
                }
            }
            
            String queryUrl = query.toString();
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            log.debug("VNPay Hash Data: {}", hashData);
            log.debug("VNPay Secure Hash: {}", vnpSecureHash);
            queryUrl += "&vnp_SecureHashType=HMACSHA512&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;

            log.info("Created VNPay payment URL for order: {}", orderId);

            return VNPayPaymentResponse.builder()
                    .code("00")
                    .message("Success")
                    .paymentUrl(paymentUrl)
                    .build();

        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding VNPay parameters", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public VNPayCallbackResponse processCallback(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            // Verify signature
            if (!verifySignature(params, vnpSecureHash)) {
                log.error("Invalid VNPay signature");
                return VNPayCallbackResponse.builder()
                        .code("97")
                        .message("Invalid signature")
                        .build();
            }

            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");
            long amount = Long.parseLong(params.get("vnp_Amount")) / 100;

            // Find deposit order
            DepositOrder depositOrder = depositOrderRepository.findByGatewayOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));

            // Store gateway response
            depositOrder.setGatewayResponse(objectMapper.writeValueAsString(params));
            depositOrder.setGatewayTxnId(transactionNo);

            // Check if payment is successful
            if ("00".equals(responseCode)) {
                // Payment successful
                depositOrder.setTxnStatus(TxnStatus.COMPLETED);
                depositOrder.setReconciledAt(LocalDateTime.now());
                depositOrderRepository.save(depositOrder);

                // Update wallet balance
                Wallet wallet = walletRepository.findByUserId(depositOrder.getUser().getId())
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

                BigDecimal oldBalance = wallet.getBalanceMxc();
                wallet.addToBalance(depositOrder.getMxcAmount());
                walletRepository.save(wallet);

                // Create wallet transaction
                UUID transactionGroupId = UUID.randomUUID();
                WalletTransaction transaction = WalletTransaction.builder()
                        .wallet(wallet)
                        .transactionGroupId(transactionGroupId)
                        .txnType(TxnType.DEPOSIT)
                        .direction(LedgerDirection.CREDIT)
                        .amountMxc(depositOrder.getMxcAmount())
                        .balanceAfterMxc(wallet.getBalanceMxc())
                        .referenceId(depositOrder.getId())
                        .referenceType("DEPOSIT_ORDER")
                        .note("Deposit via VNPay - Order: " + orderId)
                        .txnStatus(TxnStatus.COMPLETED)
                        .entryHash(generateHash(transactionGroupId.toString()))
                        .createdBy(depositOrder.getUser())
                        .build();
                walletTransactionRepository.save(transaction);

                log.info("VNPay payment successful for order: {}, amount: {} VND", orderId, amount);

                return VNPayCallbackResponse.builder()
                        .code("00")
                        .message("Payment successful")
                        .orderId(orderId)
                        .amount(BigDecimal.valueOf(amount))
                        .transactionNo(transactionNo)
                        .bankCode(bankCode)
                        .payDate(payDate)
                        .build();
            } else {
                // Payment failed
                depositOrder.setTxnStatus(TxnStatus.FAILED);
                depositOrderRepository.save(depositOrder);

                log.warn("VNPay payment failed for order: {}, response code: {}", orderId, responseCode);

                return VNPayCallbackResponse.builder()
                        .code(responseCode)
                        .message("Payment failed")
                        .orderId(orderId)
                        .amount(BigDecimal.valueOf(amount))
                        .build();
            }

        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            return VNPayCallbackResponse.builder()
                    .code("99")
                    .message("System error")
                    .build();
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> params, String secureHash) {
        String hashData = VNPayUtil.hashAllFields(params);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        return calculatedHash.equalsIgnoreCase(secureHash);
    }

    private String generateHash(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}

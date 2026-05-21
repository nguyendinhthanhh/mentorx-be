package com.mentorx.api.feature.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.PaymentGateway;
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
import com.mentorx.api.feature.wallet.repository.DepositOrderRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final DepositOrderRepository depositOrderRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    private static final Set<String> SUPPORTED_BANK_CODES = Set.of("VNPAYQR", "VNBANK", "INTCARD", "NCB");
    private static final String BASE_CURRENCY = "VND";

    @Override
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        enforceVndGatewayCurrency(request.getCurrency(), "VNPay");

        String secret = vnPayConfig.getHashSecret() == null ? "" : vnPayConfig.getHashSecret();
        String secretMask = secret.length() <= 8
                ? "****"
                : secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
        log.info("VNPay config loaded - tmnCode: {}, returnUrl: {}, hashSecret: {}", vnPayConfig.getTmnCode(), vnPayConfig.getReturnUrl(), secretMask);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String orderId = generateOrderId();
        DepositOrder depositOrder = walletService.createDepositOrder(
                user.getId(),
                request.getAmount(),
                BASE_CURRENCY,
                PaymentGateway.VNPAY,
                orderId,
                null,
                request.getOrderInfo()
        );
        return createPaymentForOrder(depositOrder, request.getBankCode(), httpRequest);
    }

    @Override
    public VNPayPaymentResponse createPaymentForOrder(DepositOrder depositOrder, String bankCode, HttpServletRequest httpRequest) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(depositOrder.getConvertedAmountVnd().multiply(BigDecimal.valueOf(100)).longValue()));
            vnpParams.put("vnp_CurrCode", BASE_CURRENCY);

            String normalizedBankCode = normalizeBankCode(bankCode);
            if (normalizedBankCode != null) {
                vnpParams.put("vnp_BankCode", normalizedBankCode);
            }

            vnpParams.put("vnp_TxnRef", depositOrder.getGatewayOrderId());
            String orderInfo = sanitizeOrderInfo(defaultOrderInfo(depositOrder));
            if (orderInfo.length() > 255) {
                orderInfo = orderInfo.substring(0, 255);
            }
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpRequest));

            TimeZone vnTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            Calendar cld = Calendar.getInstance(vnTimeZone);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(vnTimeZone);
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString());
                    if (hashData.length() > 0) {
                        hashData.append('&');
                        query.append('&');
                    }
                    hashData.append(fieldName).append('=').append(encodedValue);
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(encodedValue);
                }
            }

            String queryUrl = query.toString();
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            log.debug("VNPay Hash Data: {}", hashData);
            log.debug("VNPay Secure Hash: {}", vnpSecureHash);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;

            log.info(
                    "Created VNPay payment URL for order: {}, gateway: {}, amount: {}, currency: {}",
                    depositOrder.getGatewayOrderId(),
                    depositOrder.getGateway(),
                    depositOrder.getRealAmount(),
                    depositOrder.getRealCurrency()
            );

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

            if (!verifySignature(params, vnpSecureHash)) {
                log.error("Invalid VNPay signature");
                return VNPayCallbackResponse.builder().code("97").message("Invalid signature").build();
            }

            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");
            BigDecimal amount = BigDecimal.valueOf(Long.parseLong(params.get("vnp_Amount")) / 100);

            DepositOrder depositOrder = depositOrderRepository.findByGatewayOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));
            String gatewayPayload = objectMapper.writeValueAsString(params);

            if ("00".equals(responseCode)) {
                walletService.completeDepositOrder(
                        depositOrder,
                        transactionNo,
                        gatewayPayload,
                        "Deposit via VNPay"
                );
                log.info("VNPay payment successful for order: {}, amount: {} VND", orderId, amount);
                return VNPayCallbackResponse.builder()
                        .code("00")
                        .message("Payment successful")
                        .orderId(orderId)
                        .amount(amount)
                        .transactionNo(transactionNo)
                        .bankCode(bankCode)
                        .payDate(payDate)
                        .build();
            }

            walletService.failDepositOrder(
                    depositOrder,
                    transactionNo,
                    gatewayPayload,
                    "VNPay payment failed with response code " + responseCode
            );
            log.warn("VNPay payment failed for order: {}, response code: {}", orderId, responseCode);
            return VNPayCallbackResponse.builder()
                    .code(responseCode)
                    .message("Payment failed")
                    .orderId(orderId)
                    .amount(amount)
                    .build();
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
        String hashData = VNPayUtil.hashAllFieldsEncoded(params);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        return calculatedHash.equalsIgnoreCase(secureHash);
    }

    private void enforceVndGatewayCurrency(String currency, String gatewayName) {
        String normalized = currency == null || currency.isBlank() ? BASE_CURRENCY : currency.trim().toUpperCase(Locale.ROOT);
        if (!BASE_CURRENCY.equals(normalized)) {
            throw new AppException(ErrorCode.BAD_REQUEST, gatewayName + " currently supports only VND payments");
        }
    }

    private String normalizeBankCode(String bankCode) {
        if (bankCode == null) {
            return null;
        }
        String value = bankCode.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty() || !SUPPORTED_BANK_CODES.contains(value)) {
            return null;
        }
        return value;
    }

    private String sanitizeOrderInfo(String value) {
        if (value == null || value.isBlank()) {
            return "Nap tien vao vi MentorX";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        normalized = normalized
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? "Nap tien vao vi MentorX" : normalized;
    }

    private String defaultOrderInfo(DepositOrder depositOrder) {
        return "Nap tien vao vi MentorX - " + depositOrder.getRealAmount().stripTrailingZeros().toPlainString() + " " + depositOrder.getRealCurrency();
    }

    private String generateOrderId() {
        String ts = new SimpleDateFormat("HHmmss").format(new Date());
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return ts + rand;
    }
}

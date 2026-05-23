package com.mentorx.api.feature.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.payment.config.MomoConfig;
import com.mentorx.api.feature.payment.dto.request.MomoPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.MomoCallbackResponse;
import com.mentorx.api.feature.payment.dto.response.MomoPaymentResponse;
import com.mentorx.api.feature.payment.service.MomoService;
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
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoServiceImpl implements MomoService {

    private final MomoConfig momoConfig;
    private final DepositOrderRepository depositOrderRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_CURRENCY = "VND";

    @Override
    @Transactional
    public MomoPaymentResponse createPayment(MomoPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            enforceVndGatewayCurrency(request.getCurrency(), "MoMo");

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            String orderId = UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();
            String amount = String.valueOf(request.getAmount().longValue());
            String orderInfo = request.getOrderInfo() != null ? request.getOrderInfo() : "Nap tien MentorX via MoMo";
            String extraData = request.getExtraData() != null ? request.getExtraData() : "";
            String requestType = "captureWallet";

            walletService.createDepositOrder(
                    user.getId(),
                    request.getAmount(),
                    BASE_CURRENCY,
                    PaymentGateway.MOMO,
                    orderId,
                    null,
                    orderInfo
            );

            DepositOrder depositOrder = depositOrderRepository.findByGatewayOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));

            return createPaymentForOrder(depositOrder, httpRequest);
        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public MomoPaymentResponse createPaymentForOrder(DepositOrder depositOrder, HttpServletRequest httpRequest) {
        try {
            enforceVndGatewayCurrency(depositOrder.getRealCurrency(), "MoMo");

            String orderId = depositOrder.getGatewayOrderId();
            String requestId = UUID.randomUUID().toString();
            String amount = depositOrder.getConvertedAmountVnd().setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            String orderInfo = defaultOrderInfo(depositOrder);
            String extraData = "";
            String requestType = "captureWallet";

            String rawHash = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + momoConfig.getNotifyUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + momoConfig.getReturnUrl() +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = hmacSha256(momoConfig.getSecretKey(), rawHash);

            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", momoConfig.getPartnerCode());
            body.put("partnerName", "MentorX");
            body.put("storeId", "MentorX");
            body.put("requestId", requestId);
            body.put("amount", amount);
            body.put("orderId", orderId);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", momoConfig.getReturnUrl());
            body.put("ipnUrl", momoConfig.getNotifyUrl());
            body.put("lang", "vi");
            body.put("extraData", extraData);
            body.put("requestType", requestType);
            body.put("signature", signature);

            log.info("Requesting MoMo payment for order: {} with body: {}", orderId, body);
            MomoPaymentResponse response = restTemplate.postForObject(momoConfig.getMomoUrl(), body, MomoPaymentResponse.class);
            log.info("MoMo payment response for order {}: {}", orderId, response);

            if (response == null || !"0".equals(response.getResultCode())) {
                String errorMsg = response != null ? response.getMessage() : "Empty response from MoMo";
                log.error("MoMo payment creation failed: {}", errorMsg);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, errorMsg);
            }

            return response;
        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void processCallback(Map<String, String> params) {
        log.info("Processing MoMo IPN callback: {}", params);
        handleMomoResponse(params);
    }

    @Override
    @Transactional
    public MomoCallbackResponse processReturn(Map<String, String> params) {
        log.info("Processing MoMo return redirect: {}", params);
        handleMomoResponse(params);

        String resultCode = params.get("resultCode");
        String message = params.get("message");
        String orderId = params.get("orderId");
        String amountStr = params.get("amount");
        String transId = params.get("transId");
        String payType = params.get("payType");

        BigDecimal amount = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;

        return MomoCallbackResponse.builder()
                .resultCode(resultCode)
                .message(message)
                .orderId(orderId)
                .amount(amount)
                .transId(transId)
                .payType(payType)
                .build();
    }

    private void handleMomoResponse(Map<String, String> params) {
        String orderId = params.get("orderId");
        String resultCode = params.get("resultCode");
        String transId = params.get("transId");
        String message = params.get("message");

        DepositOrder order = depositOrderRepository.findByGatewayOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));

        if (order.getTxnStatus() != com.mentorx.api.common.enums.TxnStatus.PENDING) {
            log.info("Order {} already processed with status: {}", orderId, order.getTxnStatus());
            return;
        }

        String gatewayPayload = null;
        try {
            gatewayPayload = objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.error("Error serializing MoMo response", e);
        }

        if ("0".equals(resultCode)) {
            log.info("MoMo payment success for order: {}", orderId);
            walletService.completeDepositOrder(order, transId, gatewayPayload, "Deposit via MoMo");
            return;
        }

        log.warn("MoMo payment failed/cancelled for order: {}. Code: {}, Message: {}", orderId, resultCode, message);
        walletService.failDepositOrder(order, transId, gatewayPayload, "MoMo payment failed: " + message);
    }

    private String hmacSha256(String key, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] array = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error generating HMAC-SHA256", e);
            return null;
        }
    }

    private void enforceVndGatewayCurrency(String currency, String gatewayName) {
        String normalized = currency == null || currency.isBlank() ? BASE_CURRENCY : currency.trim().toUpperCase(Locale.ROOT);
        if (!BASE_CURRENCY.equals(normalized)) {
            throw new AppException(ErrorCode.BAD_REQUEST, gatewayName + " currently supports only VND payments");
        }
    }

    private String defaultOrderInfo(DepositOrder depositOrder) {
        return "Nap tien MentorX via MoMo - " + depositOrder.getRealAmount().stripTrailingZeros().toPlainString() + " " + depositOrder.getRealCurrency();
    }
}

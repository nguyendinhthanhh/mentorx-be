package com.mentorx.api.feature.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.payment.config.PayOSProperties;
import com.mentorx.api.feature.payment.dto.request.PayOSPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.PayOSPaymentResponse;
import com.mentorx.api.feature.payment.dto.response.PayOSReturnResponse;
import com.mentorx.api.feature.payment.service.PayOSService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import com.mentorx.api.feature.wallet.repository.DepositOrderRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final PayOSProperties payOSProperties;
    private final DepositOrderRepository depositOrderRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_CURRENCY = "VND";
    private static final String SUCCESS_CODE = "00";
    private static final String PAID_STATUS = "PAID";
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final String DEFAULT_DESCRIPTION = "MXC TOPUP";

    @Override
    @Transactional
    public PayOSPaymentResponse createPayment(PayOSPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            payOSProperties.requireConfigured();
            enforceVndGatewayCurrency(request.getCurrency(), "PayOS");

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            long orderCode = generateOrderCode();
            walletService.createDepositOrder(
                    user.getId(),
                    request.getAmount(),
                    BASE_CURRENCY,
                    PaymentGateway.PAYOS,
                    String.valueOf(orderCode),
                    null,
                    request.getOrderInfo()
            );

            DepositOrder depositOrder = depositOrderRepository.findByGatewayAndGatewayOrderId(PaymentGateway.PAYOS, String.valueOf(orderCode))
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));

            return createPaymentForOrder(depositOrder, httpRequest);
        } catch (Exception e) {
            log.error("Error creating PayOS payment", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public PayOSPaymentResponse createPaymentForOrder(DepositOrder depositOrder, HttpServletRequest httpRequest) {
        try {
            payOSProperties.requireConfigured();
            enforceVndGatewayCurrency(depositOrder.getRealCurrency(), "PayOS");

            long orderCode = parseOrderCode(depositOrder.getGatewayOrderId());
            long amount = depositOrder.getConvertedAmountVnd()
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
            String description = normalizeDescription(DEFAULT_DESCRIPTION);
            String signature = createCreatePaymentSignature(amount, description, orderCode);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", payOSProperties.resolvedCancelUrl());
            body.put("returnUrl", payOSProperties.resolvedReturnUrl());
            body.put("signature", signature);

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response;
            try {
                response = restTemplate.exchange(
                        payOSProperties.resolvedBaseUrl() + "/v2/payment-requests",
                        HttpMethod.POST,
                        entity,
                        JsonNode.class
                );
            } catch (HttpStatusCodeException ex) {
                log.error("PayOS create payment rejected. status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "PayOS rejected the payment request");
            }

            JsonNode root = response.getBody();
            if (root == null) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Empty response from PayOS");
            }

            String code = root.path("code").asText("");
            String message = root.path("desc").asText("PayOS payment request failed");
            JsonNode data = root.path("data");

            if (!SUCCESS_CODE.equals(code)) {
                log.error("PayOS create payment failed for order {}: {}", orderCode, root);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, message);
            }

            PayOSPaymentResponse paymentResponse = PayOSPaymentResponse.builder()
                    .code(code)
                    .message(message)
                    .orderCode(orderCode)
                    .paymentLinkId(textOrNull(data.path("paymentLinkId")))
                    .checkoutUrl(textOrNull(data.path("checkoutUrl")))
                    .qrCode(textOrNull(data.path("qrCode")))
                    .amount(data.path("amount").isNumber() ? data.path("amount").asLong() : amount)
                    .status(textOrNull(data.path("status")))
                    .build();

            log.info("Created PayOS payment link for order: {}, amount: {} VND", orderCode, amount);
            return paymentResponse;
        } catch (Exception e) {
            log.error("Error creating PayOS payment", e);
            if (e instanceof AppException appException) {
                throw appException;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public PayOSReturnResponse processReturn(Map<String, String> params) {
        try {
            payOSProperties.requireConfigured();

            long orderCode = parseOrderCode(params.get("orderCode"));
            boolean cancel = Boolean.parseBoolean(params.getOrDefault("cancel", "false"));
            String status = params.get("status");
            String responseCode = params.get("code");
            String paymentLinkId = params.get("id");

            DepositOrder depositOrder = findDepositOrder(orderCode);
            if (depositOrder.getTxnStatus() == TxnStatus.PENDING) {
                JsonNode paymentData = fetchPaymentRequest(orderCode);
                String verifiedStatus = textOrNull(paymentData.path("status"));
                String verifiedLinkId = textOrNull(paymentData.path("paymentLinkId"));
                String gatewayPayload = objectMapper.writeValueAsString(params);
                Long transactionId = paymentData.path("transactionId").isNumber() ? paymentData.path("transactionId").asLong() : null;

                if (PAID_STATUS.equalsIgnoreCase(verifiedStatus)) {
                    walletService.completeDepositOrder(
                            depositOrder,
                            transactionId != null ? String.valueOf(transactionId) : paymentLinkId,
                            gatewayPayload,
                            "Deposit via PayOS"
                    );
                    status = verifiedStatus;
                } else if (CANCELLED_STATUS.equalsIgnoreCase(verifiedStatus) || cancel) {
                    walletService.failDepositOrder(
                            depositOrder,
                            paymentLinkId,
                            gatewayPayload,
                            "PayOS payment cancelled"
                    );
                    status = CANCELLED_STATUS;
                }

                return PayOSReturnResponse.builder()
                        .code(SUCCESS_CODE.equals(responseCode) ? SUCCESS_CODE : "01")
                        .message(PAID_STATUS.equalsIgnoreCase(status) ? "Payment successful" : "Payment cancelled or pending")
                        .orderCode(orderCode)
                        .paymentLinkId(verifiedLinkId != null ? verifiedLinkId : paymentLinkId)
                        .status(status)
                        .cancel(cancel)
                        .amount(paymentData.path("amount").isNumber() ? paymentData.path("amount").asLong() : null)
                        .transactionId(transactionId != null ? String.valueOf(transactionId) : paymentLinkId)
                        .build();
            }

            return PayOSReturnResponse.builder()
                    .code(SUCCESS_CODE.equals(responseCode) ? SUCCESS_CODE : "01")
                    .message(depositOrder.getTxnStatus() == TxnStatus.COMPLETED
                            ? "Payment already processed"
                            : "Payment pending")
                    .orderCode(orderCode)
                    .paymentLinkId(paymentLinkId)
                    .status(status)
                    .cancel(cancel)
                    .transactionId(paymentLinkId)
                    .build();
        } catch (Exception e) {
            log.error("Error processing PayOS return", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void processWebhook(Map<String, Object> payload) {
        try {
            payOSProperties.requireConfigured();

            Object signatureObj = payload.get("signature");
            if (!(signatureObj instanceof String signature) || !verifyWebhookSignature(payload, signature)) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Invalid PayOS webhook signature");
            }

            Map<String, Object> data = asObjectMap(payload.get("data"));
            long orderCode = parseOrderCode(data.get("orderCode"));
            String code = textOrNull(data.get("code"));
            String description = textOrNull(data.get("desc"));
            String reference = textOrNull(data.get("reference"));

            DepositOrder depositOrder = findDepositOrder(orderCode);
            if (depositOrder.getTxnStatus() != TxnStatus.PENDING) {
                log.info("PayOS order {} already processed with status {}", orderCode, depositOrder.getTxnStatus());
                return;
            }

            String gatewayPayload = objectMapper.writeValueAsString(payload);
            long amount = data.get("amount") instanceof Number number ? number.longValue() : 0L;

            if (SUCCESS_CODE.equals(code)) {
                walletService.completeDepositOrder(
                        depositOrder,
                        reference,
                        gatewayPayload,
                        description != null ? description : "Deposit via PayOS"
                );
                log.info("PayOS webhook completed order {}", orderCode);
                return;
            }

            walletService.failDepositOrder(
                    depositOrder,
                    reference,
                    gatewayPayload,
                    description != null ? description : "PayOS payment failed"
            );
            log.warn("PayOS webhook failed order {}, amount {}", orderCode, amount);
        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            if (e instanceof AppException appException) {
                throw appException;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payOSProperties.clientId());
        headers.set("x-api-key", payOSProperties.apiKey());
        if (StringUtils.hasText(payOSProperties.partnerCode())) {
            headers.set("x-partner-code", payOSProperties.partnerCode().trim());
        }
        return headers;
    }

    private JsonNode fetchPaymentRequest(long orderCode) {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                payOSProperties.resolvedBaseUrl() + "/v2/payment-requests/" + orderCode,
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        JsonNode root = response.getBody();
        if (root == null || !SUCCESS_CODE.equals(root.path("code").asText(""))) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unable to verify PayOS payment status");
        }
        return root.path("data");
    }

    private DepositOrder findDepositOrder(long orderCode) {
        return depositOrderRepository.findByGatewayAndGatewayOrderId(PaymentGateway.PAYOS, String.valueOf(orderCode))
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));
    }

    private boolean verifyWebhookSignature(Map<String, Object> payload, String currentSignature) {
        Map<String, Object> data = asObjectMap(payload.get("data"));
        String rawData = toSignatureString(data);
        String calculatedSignature = hmacSha256(payOSProperties.checksumKey(), rawData);
        return calculatedSignature.equalsIgnoreCase(currentSignature);
    }

    private String createCreatePaymentSignature(long amount, String description, long orderCode) {
        String data = "amount=" + amount
                + "&cancelUrl=" + payOSProperties.resolvedCancelUrl()
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + payOSProperties.resolvedReturnUrl();
        return hmacSha256(payOSProperties.checksumKey(), data);
    }

    private String toSignatureString(Map<String, Object> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + signatureValue(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String signatureValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sortedMap = new TreeMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sortedMap.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return toSignatureString(sortedMap);
        }
        if (value instanceof List<?> list) {
            List<Object> normalized = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> itemMap) {
                    Map<String, Object> sortedMap = new TreeMap<>();
                    for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
                        sortedMap.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    normalized.add(sortedMap);
                } else {
                    normalized.add(item);
                }
            }
            try {
                return objectMapper.writeValueAsString(normalized);
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unable to serialize PayOS signature payload");
            }
        }
        if (Objects.equals(value, "undefined") || Objects.equals(value, "null")) {
            return "";
        }
        return String.valueOf(value);
    }

    private Map<String, Object> asObjectMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> result.put(String.valueOf(key), entryValue));
            return result;
        }
        return Map.of();
    }

    private String normalizeDescription(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_DESCRIPTION;
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            return DEFAULT_DESCRIPTION;
        }
        return normalized.length() > 9 ? normalized.substring(0, 9) : normalized;
    }

    private void enforceVndGatewayCurrency(String currency, String gatewayName) {
        String normalized = currency == null || currency.isBlank() ? BASE_CURRENCY : currency.trim().toUpperCase(Locale.ROOT);
        if (!BASE_CURRENCY.equals(normalized)) {
            throw new AppException(ErrorCode.BAD_REQUEST, gatewayName + " currently supports only VND payments");
        }
    }

    private long parseOrderCode(String orderCode) {
        if (!StringUtils.hasText(orderCode)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Missing PayOS orderCode");
        }
        try {
            return Long.parseLong(orderCode.trim());
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid PayOS orderCode");
        }
    }

    private long parseOrderCode(Object orderCode) {
        if (orderCode instanceof Number number) {
            return number.longValue();
        }
        return parseOrderCode(orderCode != null ? String.valueOf(orderCode) : null);
    }

    private long generateOrderCode() {
        long base = Instant.now().toEpochMilli() * 10L;
        long suffix = ThreadLocalRandom.current().nextInt(10);
        return base + suffix;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        String value = node.asText(null);
        return StringUtils.hasText(value) ? value : null;
    }

    private String textOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = value instanceof String str ? str : String.valueOf(value);
        return StringUtils.hasText(text) ? text : null;
    }

    private String hmacSha256(String key, String data) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] array = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unable to calculate PayOS signature");
        }
    }
}

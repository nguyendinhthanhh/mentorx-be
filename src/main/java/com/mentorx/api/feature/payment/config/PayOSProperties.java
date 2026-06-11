package com.mentorx.api.feature.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "payos")
public record PayOSProperties(
        String clientId,
        String apiKey,
        String checksumKey,
        String baseUrl,
        String returnUrl,
        String cancelUrl,
        String webhookUrl,
        String partnerCode
) {

    public String resolvedBaseUrl() {
        return StringUtils.hasText(baseUrl) ? baseUrl.trim().replaceAll("/+$", "") : "https://api-merchant.payos.vn";
    }

    public String resolvedReturnUrl() {
        return StringUtils.hasText(returnUrl) ? returnUrl.trim() : "http://localhost:3000/payment/payos-return";
    }

    public String resolvedCancelUrl() {
        return StringUtils.hasText(cancelUrl) ? cancelUrl.trim() : "http://localhost:3000/payment/payos-return";
    }

    public String resolvedWebhookUrl() {
        return StringUtils.hasText(webhookUrl) ? webhookUrl.trim() : "http://localhost:8080/api/v1/payment/payos/webhook";
    }

    public void requireConfigured() {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("payos.client-id (or PAYOS_CLIENT_ID) is required");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("payos.api-key (or PAYOS_API_KEY) is required");
        }
        if (!StringUtils.hasText(checksumKey)) {
            throw new IllegalStateException("payos.checksum-key (or PAYOS_CHECKSUM_KEY) is required");
        }
    }
}

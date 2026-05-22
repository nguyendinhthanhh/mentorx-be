package com.mentorx.api.feature.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositCreateRequest(
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @DecimalMin(value = "10000", message = "Minimum deposit is 10,000 VND")
        BigDecimal amountVnd,

        String currency,

        String bankCode,

        @NotNull(message = "Gateway is required")
        String gateway
) {
        public BigDecimal resolvedAmount() {
                return amount != null ? amount : amountVnd;
        }

        public String resolvedCurrency() {
                return currency != null && !currency.isBlank() ? currency : "VND";
        }
}

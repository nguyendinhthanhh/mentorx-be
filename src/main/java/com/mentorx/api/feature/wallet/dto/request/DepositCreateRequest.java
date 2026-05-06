package com.mentorx.api.feature.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositCreateRequest(
        @NotNull(message = "Amount VND is required")
        @DecimalMin(value = "10000", message = "Minimum deposit is 10,000 VND")
        BigDecimal amountVnd,

        @NotNull(message = "Gateway is required")
        String gateway
) {
}

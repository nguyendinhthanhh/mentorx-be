package com.mentorx.api.feature.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConversionPreviewRequest(
        @NotNull(message = "Original amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal originalAmount,

        @NotBlank(message = "Original currency is required")
        String originalCurrency
) {
}

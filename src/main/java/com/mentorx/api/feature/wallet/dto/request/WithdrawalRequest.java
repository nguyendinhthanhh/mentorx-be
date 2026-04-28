package com.mentorx.api.feature.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Withdrawal method is required")
        String withdrawalMethod,

        String bankAccount,

        String paypalEmail
) {
}
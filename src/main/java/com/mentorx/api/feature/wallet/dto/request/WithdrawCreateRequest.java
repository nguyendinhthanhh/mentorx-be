package com.mentorx.api.feature.wallet.dto.request;

import com.mentorx.api.common.enums.PayoutMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WithdrawCreateRequest(
        @NotNull(message = "MXC amount is required")
        @DecimalMin(value = "100", message = "Minimum withdrawal is 100 MXC")
        BigDecimal mxcAmount,

        @NotBlank(message = "Bank name is required")
        String bankName,

        @NotBlank(message = "Bank account number is required")
        String bankAccountNo,

        @NotBlank(message = "Bank account name is required")
        String bankAccountName,

        @Size(max = 10, message = "Payout country must not exceed 10 characters")
        String payoutCountry,

        PayoutMethod payoutMethod,

        @Size(max = 255, message = "Payout reference must not exceed 255 characters")
        String payoutReference
) {
}

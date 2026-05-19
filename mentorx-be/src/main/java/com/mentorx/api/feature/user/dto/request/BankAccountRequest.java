package com.mentorx.api.feature.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BankAccountRequest(
        @NotBlank(message = "Bank name is required")
        @Size(max = 100, message = "Bank name must not exceed 100 characters")
        String bankName,

        @Size(max = 50, message = "Bank code must not exceed 50 characters")
        String bankCode,

        @NotBlank(message = "Account number is required")
        @Size(max = 50, message = "Account number must not exceed 50 characters")
        String accountNumber,

        @NotBlank(message = "Account holder name is required")
        @Size(max = 200, message = "Account holder name must not exceed 200 characters")
        String accountHolderName,

        @Size(max = 200, message = "Branch name must not exceed 200 characters")
        String branchName,

        Boolean isDefault,

        String notes
) {
}

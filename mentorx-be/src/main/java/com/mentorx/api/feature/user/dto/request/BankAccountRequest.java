package com.mentorx.api.feature.user.dto.request;

import com.mentorx.api.common.enums.PayoutMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
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

        @NotBlank(message = "Payout country is required")
        @Size(max = 10, message = "Payout country must not exceed 10 characters")
        String payoutCountry,

        PayoutMethod payoutMethod,

        @Size(max = 80, message = "IBAN must not exceed 80 characters")
        String iban,

        @Size(max = 40, message = "SWIFT code must not exceed 40 characters")
        String swiftCode,

        @Email(message = "PayPal email must be valid")
        String paypalEmail,

        @Email(message = "Wise email must be valid")
        String wiseEmail,

        @Size(max = 255, message = "Stripe account ID must not exceed 255 characters")
        String stripeConnectAccountId,

        Boolean isDefault,

        String notes
) {
}

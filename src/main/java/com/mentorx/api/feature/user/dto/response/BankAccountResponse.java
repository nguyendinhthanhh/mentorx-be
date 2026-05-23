package com.mentorx.api.feature.user.dto.response;

import com.mentorx.api.common.enums.PayoutMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record BankAccountResponse(
        UUID id,
        UUID userId,
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolderName,
        String branchName,
        String payoutCountry,
        PayoutMethod payoutMethod,
        String iban,
        String swiftCode,
        String paypalEmail,
        String wiseEmail,
        String stripeConnectAccountId,
        Boolean isDefault,
        Boolean isVerified,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime verifiedAt,

        String verifiedBy,
        String notes,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}

package com.mentorx.api.feature.user.dto.request;

import com.mentorx.api.common.enums.IdentityDocumentType;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MentorIdentityVerificationRequest(
        @Size(max = 150, message = "Legal name must not exceed 150 characters")
        String legalName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 100, message = "Country of residence must not exceed 100 characters")
        String countryOfResidence,

        IdentityDocumentType documentType,

        @Size(max = 40, message = "Document number must not exceed 40 characters")
        String documentNumber,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber
) {
}

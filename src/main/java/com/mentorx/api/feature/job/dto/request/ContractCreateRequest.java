package com.mentorx.api.feature.job.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractCreateRequest(
        @NotNull UUID jobId,
        UUID proposalId,
        @NotNull UUID clientId,
        @NotNull UUID mentorId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotNull @DecimalMin("0.0") BigDecimal totalAmount,
        @DecimalMin("0.0") BigDecimal hourlyRate,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 5000) String termsAndConditions,
        @Size(max = 1000) String paymentTerms,
        @Size(max = 2000) String deliverables,
        Boolean isRenewable,
        Boolean autoRenewal,
        @Size(max = 1000) String renewalTerms,
        Boolean ndaRequired
) {}

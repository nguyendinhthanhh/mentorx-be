package com.mentorx.api.feature.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoPaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum amount is 10,000 VND")
    private BigDecimal amount;

    private String orderInfo;
    
    private String extraData; // Base64 encoded JSON string

    @Builder.Default
    private String currency = "VND";
}

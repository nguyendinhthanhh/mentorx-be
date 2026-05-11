package com.mentorx.api.feature.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayPaymentResponse {

    private String code;
    private String message;
    private String paymentUrl;
}

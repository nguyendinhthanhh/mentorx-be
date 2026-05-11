package com.mentorx.api.feature.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoPaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String responseTime;
    private String message;
    private String resultCode;
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;
}

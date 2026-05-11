package com.mentorx.api.feature.payment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MomoCallbackResponse {
    private String resultCode;
    private String message;
    private String orderId;
    private BigDecimal amount;
    private String transId;
    private String payType;
}

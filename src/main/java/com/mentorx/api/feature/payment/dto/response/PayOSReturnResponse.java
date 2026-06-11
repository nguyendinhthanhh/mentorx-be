package com.mentorx.api.feature.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSReturnResponse {

    private String code;
    private String message;
    private Long orderCode;
    private String paymentLinkId;
    private String status;
    private Boolean cancel;
    private Long amount;
    private String transactionId;
}

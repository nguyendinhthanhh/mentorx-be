package com.mentorx.api.feature.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayCallbackResponse {

    private String code;
    private String message;
    private String orderId;
    private BigDecimal amount;
    private String transactionNo;
    private String bankCode;
    private String payDate;
}

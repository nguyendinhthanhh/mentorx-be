package com.mentorx.api.feature.payment.service;

import com.mentorx.api.feature.payment.dto.request.PayOSPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.PayOSPaymentResponse;
import com.mentorx.api.feature.payment.dto.response.PayOSReturnResponse;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PayOSService {
    PayOSPaymentResponse createPayment(PayOSPaymentRequest request, HttpServletRequest httpRequest);

    PayOSPaymentResponse createPaymentForOrder(DepositOrder depositOrder, HttpServletRequest httpRequest);

    PayOSReturnResponse processReturn(Map<String, String> params);

    void processWebhook(Map<String, Object> payload);
}

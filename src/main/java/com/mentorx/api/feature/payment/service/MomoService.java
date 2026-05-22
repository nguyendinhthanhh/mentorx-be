package com.mentorx.api.feature.payment.service;

import com.mentorx.api.feature.payment.dto.request.MomoPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.MomoCallbackResponse;

import com.mentorx.api.feature.payment.dto.response.MomoPaymentResponse;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface MomoService {
    MomoPaymentResponse createPayment(MomoPaymentRequest request, HttpServletRequest httpRequest);
    MomoPaymentResponse createPaymentForOrder(DepositOrder depositOrder, HttpServletRequest httpRequest);
    void processCallback(Map<String, String> params);
    MomoCallbackResponse processReturn(Map<String, String> params);
}

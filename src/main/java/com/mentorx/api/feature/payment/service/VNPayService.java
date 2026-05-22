package com.mentorx.api.feature.payment.service;

import com.mentorx.api.feature.payment.dto.request.VNPayPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.VNPayCallbackResponse;
import com.mentorx.api.feature.payment.dto.response.VNPayPaymentResponse;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {

    /**
     * Create VNPay payment URL
     */
    VNPayPaymentResponse createPayment(VNPayPaymentRequest request, HttpServletRequest httpRequest);

    VNPayPaymentResponse createPaymentForOrder(DepositOrder depositOrder, String bankCode, HttpServletRequest httpRequest);

    /**
     * Process VNPay callback/return
     */
    VNPayCallbackResponse processCallback(Map<String, String> params);

    /**
     * Verify VNPay signature
     */
    boolean verifySignature(Map<String, String> params, String secureHash);
}

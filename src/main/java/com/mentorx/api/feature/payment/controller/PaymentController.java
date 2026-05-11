package com.mentorx.api.feature.payment.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.payment.dto.request.VNPayPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.VNPayCallbackResponse;
import com.mentorx.api.feature.payment.dto.response.VNPayPaymentResponse;
import com.mentorx.api.feature.payment.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment APIs")
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL")
    public ResponseEntity<ApiResponse<VNPayPaymentResponse>> createVNPayPayment(
            @Valid @RequestBody VNPayPaymentRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating VNPay payment for amount: {}", request.getAmount());
        VNPayPaymentResponse response = vnPayService.createPayment(request, httpRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Payment URL created successfully", response));
    }

    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay payment callback/return URL")
    public ResponseEntity<ApiResponse<VNPayCallbackResponse>> vnpayCallback(
            @RequestParam Map<String, String> params) {
        
        log.info("Received VNPay callback with params: {}", params.keySet());
        VNPayCallbackResponse response = vnPayService.processCallback(params);
        
        return ResponseEntity.ok(ApiResponse.success("Callback processed", response));
    }

    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPay payment return URL (for frontend redirect)")
    public ResponseEntity<ApiResponse<VNPayCallbackResponse>> vnpayReturn(
            @RequestParam Map<String, String> params) {
        
        log.info("Received VNPay return with params: {}", params.keySet());
        VNPayCallbackResponse response = vnPayService.processCallback(params);
        
        // Frontend can handle this response to show success/failure page
        return ResponseEntity.ok(ApiResponse.success("Payment processed", response));
    }
}

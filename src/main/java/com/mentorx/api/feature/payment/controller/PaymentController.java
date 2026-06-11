package com.mentorx.api.feature.payment.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.payment.dto.request.PayOSPaymentRequest;
import com.mentorx.api.feature.payment.dto.request.MomoPaymentRequest;
import com.mentorx.api.feature.payment.dto.request.VNPayPaymentRequest;
import com.mentorx.api.feature.payment.dto.response.PayOSPaymentResponse;
import com.mentorx.api.feature.payment.dto.response.PayOSReturnResponse;
import com.mentorx.api.feature.payment.dto.response.MomoCallbackResponse;
import com.mentorx.api.feature.payment.dto.response.MomoPaymentResponse;
import com.mentorx.api.feature.payment.dto.response.VNPayCallbackResponse;
import com.mentorx.api.feature.payment.dto.response.VNPayPaymentResponse;
import com.mentorx.api.feature.payment.service.PayOSService;
import com.mentorx.api.feature.payment.service.MomoService;
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
    private final MomoService momoService;
    private final PayOSService payOSService;

    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL")
    public ResponseEntity<ApiResponse<VNPayPaymentResponse>> createVNPayPayment(
            @Valid @RequestBody VNPayPaymentRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating VNPay payment for amount: {}", request.getAmount());
        VNPayPaymentResponse response = vnPayService.createPayment(request, httpRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Payment URL created successfully", response));
    }

    @PostMapping("/momo/create")
    @Operation(summary = "Create MoMo payment URL")
    public ResponseEntity<ApiResponse<MomoPaymentResponse>> createMomoPayment(
            @Valid @RequestBody MomoPaymentRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating MoMo payment for amount: {}", request.getAmount());
        MomoPaymentResponse response = momoService.createPayment(request, httpRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Payment URL created successfully", response));
    }

    @PostMapping("/payos/create")
    @Operation(summary = "Create PayOS payment URL")
    public ResponseEntity<ApiResponse<PayOSPaymentResponse>> createPayOSPayment(
            @Valid @RequestBody PayOSPaymentRequest request,
            HttpServletRequest httpRequest) {

        log.info("Creating PayOS payment for amount: {}", request.getAmount());
        PayOSPaymentResponse response = payOSService.createPayment(request, httpRequest);

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

    @PostMapping("/momo/callback")
    @Operation(summary = "MoMo payment callback (IPN)")
    public ResponseEntity<ApiResponse<Void>> momoCallback(
            @RequestBody Map<String, String> params) {
        
        log.info("Received MoMo IPN callback with params: {}", params);
        momoService.processCallback(params);
        
        return ResponseEntity.ok(ApiResponse.success("Callback received", null));
    }

    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPay payment return URL (for frontend redirect)")
    public ResponseEntity<ApiResponse<VNPayCallbackResponse>> vnpayReturn(
            @RequestParam Map<String, String> params) {
        
        log.info("Received VNPay return with params: {}", params.keySet());
        VNPayCallbackResponse response = vnPayService.processCallback(params);
        
        return ResponseEntity.ok(ApiResponse.success("Payment processed", response));
    }

    @GetMapping("/momo/return")
    @Operation(summary = "MoMo payment return URL (for frontend redirect)")
    public ResponseEntity<ApiResponse<MomoCallbackResponse>> momoReturn(
            @RequestParam Map<String, String> params) {
        
        log.info("Received MoMo return with params: {}", params.keySet());
        MomoCallbackResponse response = momoService.processReturn(params);
        
        return ResponseEntity.ok(ApiResponse.success("Payment processed", response));
    }

    @GetMapping("/payos/return")
    @Operation(summary = "PayOS payment return URL (for frontend redirect)")
    public ResponseEntity<ApiResponse<PayOSReturnResponse>> payOSReturn(
            @RequestParam Map<String, String> params) {

        log.info("Received PayOS return with params: {}", params.keySet());
        PayOSReturnResponse response = payOSService.processReturn(params);

        return ResponseEntity.ok(ApiResponse.success("Payment processed", response));
    }

    @PostMapping("/payos/webhook")
    @Operation(summary = "PayOS payment webhook")
    public ResponseEntity<ApiResponse<Void>> payOSWebhook(
            @RequestBody Map<String, Object> payload) {

        log.info("Received PayOS webhook with keys: {}", payload.keySet());
        payOSService.processWebhook(payload);

        return ResponseEntity.ok(ApiResponse.success("Webhook received", null));
    }

}

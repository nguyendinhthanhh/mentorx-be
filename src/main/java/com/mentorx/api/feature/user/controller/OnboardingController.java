package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.user.onboarding.dto.request.BaseStepRequest;
import com.mentorx.api.feature.user.onboarding.dto.response.OnboardingCompleteResponse;
import com.mentorx.api.feature.user.onboarding.dto.response.OnboardingProgressResponse;
import com.mentorx.api.feature.user.onboarding.dto.response.OnboardingStepResponse;
import com.mentorx.api.feature.user.onboarding.service.OnboardingCompleteService;
import com.mentorx.api.feature.user.onboarding.service.OnboardingFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingFlowService onboardingFlowService;
    private final OnboardingCompleteService onboardingCompleteService;

    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<OnboardingProgressResponse>> getProgress() {
        return ResponseEntity.ok(ApiResponse.success(onboardingFlowService.getProgress()));
    }

    @PostMapping("/step")
    public ResponseEntity<ApiResponse<OnboardingStepResponse>> processStep(
            @Valid @RequestBody BaseStepRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Step processed", onboardingFlowService.processStep(request)));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<OnboardingCompleteResponse>> complete() {
        return ResponseEntity.ok(ApiResponse.success("Onboarding finalized", onboardingCompleteService.complete()));
    }

    @PostMapping("/skip")
    public ResponseEntity<ApiResponse<Map<String, Object>>> skip() {
        onboardingFlowService.skip();
        return ResponseEntity.ok(ApiResponse.success("Onboarding skipped", Map.of("skipped", true)));
    }
}

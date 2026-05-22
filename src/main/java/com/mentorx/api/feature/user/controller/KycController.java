package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.user.dto.KycStatusResponse;
import com.mentorx.api.feature.user.dto.KycSubmitRequest;
import com.mentorx.api.feature.user.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Know Your Customer (Identity Verification) APIs")
@Slf4j
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit KYC documents for identity verification")
    public ResponseEntity<KycStatusResponse> submitKyc(@ModelAttribute KycSubmitRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Received KYC submission request for user: {}", currentUserId);
        KycStatusResponse response = kycService.submitKyc(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's KYC status")

    public ResponseEntity<KycStatusResponse> getKycStatus() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        KycStatusResponse response = kycService.getKycStatus(currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{mentorProfileId}/review")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Admin review of a mentor's KYC submission")
    public ResponseEntity<Void> reviewKyc(
            @PathVariable UUID mentorProfileId,
            @RequestBody ReviewRequest reviewRequest) {
        
        UUID adminId = SecurityUtils.getCurrentUserId();
        kycService.adminReviewKyc(
                mentorProfileId, 
                reviewRequest.approved(), 
                reviewRequest.rejectionReason(), 
                adminId
        );
        return ResponseEntity.noContent().build();
    }

    public record ReviewRequest(boolean approved, String rejectionReason) {}
}

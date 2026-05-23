package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.user.dto.KycStatusResponse;
import com.mentorx.api.feature.user.dto.KycSubmitRequest;
import com.mentorx.api.feature.user.dto.request.BankAccountRequest;
import com.mentorx.api.feature.user.dto.request.MentorProfileRequest;
import com.mentorx.api.feature.user.dto.response.BankAccountResponse;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.UserBankAccount;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserBankAccountRepository;
import com.mentorx.api.feature.user.service.KycService;
import com.mentorx.api.feature.user.service.MentorProfileService;
import com.mentorx.api.feature.user.service.UserBankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentor-verification")
@RequiredArgsConstructor
@Tag(name = "Mentor Verification", description = "Risk-based mentor verification APIs")
public class MentorVerificationController {

    private final MentorModeAccessService mentorModeAccessService;
    private final MentorProfileService mentorProfileService;
    private final KycService kycService;
    private final UserBankAccountService userBankAccountService;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserBankAccountRepository userBankAccountRepository;

    @PostMapping("/professional-profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create or update the current user's professional mentor profile")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> upsertProfessionalProfile(
            @Valid @RequestBody MentorProfileRequest request
    ) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse response = mentorProfileRepository.findByUserId(currentUserId)
                .map(profile -> mentorProfileService.updateMentorProfile(currentUserId, request))
                .orElseGet(() -> mentorProfileService.createMentorProfile(currentUserId, request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Professional profile submitted successfully", response));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the current user's mentor verification profile")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getCurrentVerificationProfile() {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse response = mentorProfileService.getMentorProfileByUserId(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/identity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit identity verification for the current user")
    public ResponseEntity<ApiResponse<KycStatusResponse>> submitIdentityVerification(
            @ModelAttribute KycSubmitRequest request
    ) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        KycStatusResponse response = kycService.submitKyc(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Identity verification submitted successfully", response));
    }

    @GetMapping("/identity/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get identity verification status for the current user")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getIdentityVerificationStatus() {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        KycStatusResponse response = kycService.getKycStatus(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/payout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create or update payout setup for the current user")
    public ResponseEntity<ApiResponse<BankAccountResponse>> upsertPayoutSetup(
            @Valid @RequestBody BankAccountRequest request
    ) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        BankAccountResponse response = userBankAccountRepository.findByUserIdAndIsDefaultTrue(currentUserId)
                .map(account -> userBankAccountService.update(account.getId(), currentUserId, request))
                .orElseGet(() -> userBankAccountService.create(currentUserId, request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payout setup submitted successfully", response));
    }

    @GetMapping("/payout/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payout verification status for the current user")
    public ResponseEntity<ApiResponse<PayoutStatusResponse>> getPayoutStatus() {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        MentorProfile profile = mentorProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        UserBankAccount defaultAccount = userBankAccountRepository.findByUserIdAndIsDefaultTrue(currentUserId).orElse(null);
        BankAccountResponse accountResponse = defaultAccount == null ? null : userBankAccountService.getById(defaultAccount.getId(), currentUserId);
        return ResponseEntity.ok(ApiResponse.success(new PayoutStatusResponse(
                profile.getPayoutStatus(),
                profile.getPayoutRejectionReason(),
                accountResponse
        )));
    }

    public record PayoutStatusResponse(
            VerificationStatus status,
            String rejectionReason,
            BankAccountResponse defaultAccount
    ) {}
}

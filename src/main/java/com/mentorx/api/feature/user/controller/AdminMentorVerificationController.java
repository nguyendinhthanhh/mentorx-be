package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.util.SecurityUtils;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Mentor Verification", description = "Admin and moderator review APIs for mentor verification")
public class AdminMentorVerificationController {

    private final MentorProfileService mentorProfileService;
    private final MentorProfileRepository mentorProfileRepository;
    private final KycService kycService;
    private final UserBankAccountService userBankAccountService;
    private final UserBankAccountRepository userBankAccountRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @GetMapping("/mentor-applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "List mentor applications pending expertise review")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getMentorApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MentorProfileResponse> response = mentorProfileService.getPendingMentorApplications(
                PageRequest.of(page, size, Sort.by("createdAt").ascending())
        ).map(this::sanitizeProfile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mentor-identity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "List identity verification reviews")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getIdentityReviewQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MentorProfileResponse> response = mentorProfileRepository.findByIdentityStatuses(
                        List.of(VerificationStatus.PENDING, VerificationStatus.REJECTED, VerificationStatus.NEEDS_MORE_INFO),
                        PageRequest.of(page, size, Sort.by("updatedAt").descending())
                )
                .map(mentorProfileService::toResponse)
                .map(this::sanitizeProfile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mentor-payouts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List payout review queue")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getPayoutQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MentorProfileResponse> response = mentorProfileRepository.findByPayoutStatuses(
                        List.of(VerificationStatus.PENDING, VerificationStatus.REJECTED, VerificationStatus.NEEDS_MORE_INFO),
                        PageRequest.of(page, size, Sort.by("updatedAt").descending())
                )
                .map(mentorProfileService::toResponse)
                .map(this::sanitizeProfile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mentor-applications/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Get a single mentor application by user ID")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorApplication(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(sanitizeProfile(mentorProfileService.getMentorProfileByUserId(userId))));
    }

    @PostMapping("/mentor-applications/{userId}/approve-expertise")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Approve professional profile and expertise review")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> approveExpertise(@PathVariable UUID userId) {
        UUID reviewerId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Expertise approved successfully",
                sanitizeProfile(mentorProfileService.approveMentorApplication(userId, reviewerId))
        ));
    }

    @PostMapping("/mentor-applications/{userId}/reject-expertise")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Reject professional profile and expertise review")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> rejectExpertise(
            @PathVariable UUID userId,
            @RequestBody ModerationReasonRequest request
    ) {
        UUID reviewerId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Expertise rejected successfully",
                sanitizeProfile(mentorProfileService.rejectMentorApplication(userId, request.reason(), reviewerId))
        ));
    }

    @PostMapping("/mentor-applications/{userId}/request-more-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Request more information for mentor application")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> requestMoreInfo(
            @PathVariable UUID userId,
            @RequestBody ModerationReasonRequest request
    ) {
        UUID reviewerId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Additional information requested successfully",
                sanitizeProfile(mentorProfileService.requestMentorApplicationRevision(userId, request.reason(), reviewerId))
        ));
    }

    @PostMapping("/mentor-applications/{userId}/approve-mentor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Approve mentor mode access")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> approveMentor(@PathVariable UUID userId) {
        UUID reviewerId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Mentor approved successfully",
                sanitizeProfile(mentorProfileService.approveMentorApplication(userId, reviewerId))
        ));
    }

    @PostMapping("/mentor-applications/{userId}/reject-mentor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Reject mentor mode access")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> rejectMentor(
            @PathVariable UUID userId,
            @RequestBody ModerationReasonRequest request
    ) {
        UUID reviewerId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Mentor rejected successfully",
                sanitizeProfile(mentorProfileService.rejectMentorApplication(userId, request.reason(), reviewerId))
        ));
    }

    @PostMapping("/mentors/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Suspend an approved mentor")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> suspendMentor(
            @PathVariable UUID userId,
            @RequestBody ModerationReasonRequest request
    ) {
        UUID reviewerId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Mentor suspended successfully",
                sanitizeProfile(mentorProfileService.suspendMentor(userId, request.reason(), reviewerId))
        ));
    }

    @PostMapping("/mentor-identity/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Approve identity verification")
    public ResponseEntity<ApiResponse<String>> approveIdentity(@PathVariable UUID userId) {
        UUID reviewerId = SecurityUtils.getCurrentUserId();
        kycService.adminReviewKyc(findMentorProfileId(userId), true, null, reviewerId);
        return ResponseEntity.ok(ApiResponse.success("Identity approved successfully"));
    }

    @PostMapping("/mentor-identity/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Reject identity verification")
    public ResponseEntity<ApiResponse<String>> rejectIdentity(
            @PathVariable UUID userId,
            @RequestBody ModerationReasonRequest request
    ) {
        UUID reviewerId = SecurityUtils.getCurrentUserId();
        kycService.adminReviewKyc(findMentorProfileId(userId), false, request.reason(), reviewerId);
        return ResponseEntity.ok(ApiResponse.success("Identity rejected successfully"));
    }

    @GetMapping("/mentor-payout-accounts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List raw payout destination records")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> listMentorPayoutAccounts() {
        List<BankAccountResponse> responses = userBankAccountRepository.findAll().stream()
                .map(account -> userBankAccountService.getById(account.getId(), account.getUser().getId()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/mentor-payouts/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve payout setup")
    public ResponseEntity<ApiResponse<BankAccountResponse>> approvePayout(@PathVariable UUID userId) {
        UserBankAccount account = userBankAccountRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFAULT_BANK_ACCOUNT_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(
                "Payout setup approved successfully",
                userBankAccountService.verifyAccount(account.getId(), SecurityUtils.getCurrentUserId().toString())
        ));
    }

    @PostMapping("/mentor-payouts/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject payout setup")
    public ResponseEntity<ApiResponse<BankAccountResponse>> rejectPayout(
            @PathVariable UUID userId,
            @RequestBody ModerationReasonRequest request
    ) {
        UserBankAccount account = userBankAccountRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFAULT_BANK_ACCOUNT_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(
                "Payout setup rejected successfully",
                userBankAccountService.rejectAccount(account.getId(), SecurityUtils.getCurrentUserId().toString(), request.reason())
        ));
    }

    private UUID findMentorProfileId(UUID userId) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        return profile.getId();
    }

    private MentorProfileResponse sanitizeProfile(MentorProfileResponse profile) {
        return new MentorProfileResponse(
                profile.id(),
                profile.userId(),
                profile.user(),
                profile.headline(),
                profile.hourlyRateMxc(),
                profile.yearsOfExperience(),
                profile.availability(),
                profile.responseTimeHours(),
                profile.totalJobsDone(),
                profile.successRate(),
                profile.averageRating(),
                profile.totalReviews(),
                profile.isFeatured(),
                profile.cvUrl(),
                profile.portfolioUrl(),
                profile.videoIntroUrl(),
                profile.location(),
                profile.languages(),
                profile.expertiseStatus(),
                profile.expertiseReviewNote(),
                profile.expertiseRejectionReason(),
                profile.expertiseReviewedBy(),
                profile.expertiseReviewedByName(),
                profile.expertiseReviewedAt(),
                profile.resubmissionAllowed(),
                profile.legalName(),
                profile.dateOfBirth(),
                profile.countryOfResidence(),
                profile.identityStatus(),
                profile.identityRequired(),
                profile.identityDocumentType(),
                profile.documentNumberMasked(),
                profile.identityVerifiedAt(),
                profile.identityVerifiedBy(),
                profile.identityVerifiedByName(),
                profile.identityRejectionReason(),
                profile.verificationProvider(),
                profile.phoneNumber(),
                profile.phoneVerified(),
                profile.currentTitle(),
                profile.currentCompany(),
                profile.primaryDomain(),
                profile.skills(),
                profile.professionalBio(),
                profile.helpDescription(),
                profile.linkedinUrl(),
                profile.githubUrl(),
                profile.portfolioEvidenceUrl(),
                profile.proofLinks(),
                profile.certificateUrl(),
                profile.payoutStatus(),
                profile.payoutAccountHolderName(),
                profile.payoutBankName(),
                profile.payoutAccountNumberMasked(),
                profile.payoutCountry(),
                profile.payoutMethod(),
                profile.iban(),
                profile.swiftCode(),
                profile.paypalEmail(),
                profile.wiseEmail(),
                profile.stripeConnectAccountId(),
                profile.payoutRejectionReason(),
                profile.payoutReviewedBy(),
                profile.payoutReviewedByName(),
                profile.payoutReviewedAt(),
                profile.mentorAgreementAccepted(),
                profile.disputePolicyAccepted(),
                profile.submittedAt(),
                profile.approvedBy(),
                profile.approvedByName(),
                profile.approvedAt(),
                profile.rejectionReason(),
                profile.createdAt(),
                profile.updatedAt()
        );
    }

    public record ModerationReasonRequest(String reason) {}
}

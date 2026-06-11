package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.service.EmailService;
import com.mentorx.api.feature.matching.entity.UserSave;
import com.mentorx.api.feature.matching.repository.UserSaveRepository;
import com.mentorx.api.feature.user.dto.request.MentorProfileRequest;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserBankAccount;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.model.ProofLinkItem;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserBankAccountRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.user.service.MentorProfileService;
import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.enums.NotificationType;
import com.mentorx.api.feature.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorProfileServiceImpl implements MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSaveRepository userSaveRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserBankAccountRepository userBankAccountRepository;
    private final MentorModeAccessService mentorModeAccessService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final Environment environment;

    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;
    private static final String MENTOR_SAVE_TARGET_TYPE = "MENTOR_PROFILE";

    @Override
    @Transactional
    public MentorProfileResponse createMentorProfile(UUID userId, MentorProfileRequest request) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        User user = findUser(userId);
        if (mentorProfileRepository.findByUserId(userId).isPresent()) {
            throw new AppException(ErrorCode.MENTOR_APPLICATION_ALREADY_EXISTS);
        }

        MentorProfile profile = MentorProfile.builder()
                .user(user)
                .headline(request.headline())
                .hourlyRateMxc(request.hourlyRateMxc())
                .yearsOfExperience(request.yearsOfExperience())
                .availability(request.availability())
                .cvUrl(request.cvUrl())
                .portfolioUrl(request.portfolioUrl())
                .build();
        profile.setVideoIntroUrl(request.videoIntroUrl());
        profile.setLocation(request.location());
        profile.setLanguages(request.languages());
        applyProfessionalFields(profile, request, false);
        validateMentorApplicationPayload(profile);

        if (user.getMentorStatus() != MentorStatus.APPROVED) {
            user.setMentorStatus(MentorStatus.PENDING);
            user.setIsMentor(false);
            profile.setExpertiseStatus(VerificationStatus.PENDING);
        }
        userRepository.save(user);
        return toResponse(mentorProfileRepository.save(profile));
    }

    @Override
    public MentorProfileResponse getMentorProfileByUserId(UUID userId) {
        return toResponse(findMentorProfileByUserId(userId));
    }

    @Override
    @Transactional
    public MentorProfileResponse updateMentorProfile(UUID userId, MentorProfileRequest request) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        MentorProfile profile = findMentorProfileByUserId(userId);
        boolean isAdminOverride = mentorModeAccessService.isCurrentUserAdmin();
        if (!isAdminOverride && !canEditSubmittedProfile(profile)) {
            throw new AppException(
                    ErrorCode.ACCESS_DENIED,
                    "Your mentor profile is locked while under review. You can edit it only when moderators request more information."
            );
        }

        if (request.headline() != null) profile.setHeadline(request.headline());
        if (request.hourlyRateMxc() != null) profile.setHourlyRateMxc(request.hourlyRateMxc());
        if (request.yearsOfExperience() != null) profile.setYearsOfExperience(request.yearsOfExperience());
        if (request.availability() != null) profile.setAvailability(request.availability());
        if (request.cvUrl() != null) profile.setCvUrl(request.cvUrl());
        if (request.portfolioUrl() != null) profile.setPortfolioUrl(request.portfolioUrl());
        if (request.videoIntroUrl() != null) profile.setVideoIntroUrl(request.videoIntroUrl());
        if (request.location() != null) profile.setLocation(request.location());
        if (request.languages() != null) profile.setLanguages(request.languages());
        applyProfessionalFields(profile, request, true);
        validateMentorApplicationPayload(profile);

        User user = profile.getUser();
        if (user.getMentorStatus() == MentorStatus.REJECTED) {
            user.setMentorStatus(MentorStatus.PENDING);
            user.setIsMentor(false);
            profile.setExpertiseStatus(VerificationStatus.PENDING);
            userRepository.save(user);
            profile.setApprovedBy(null);
            profile.setApprovedAt(null);
            profile.setRejectionReason(null);
            profile.setExpertiseReviewNote(null);
            profile.setExpertiseRejectionReason(null);
            profile.setExpertiseReviewedAt(null);
            profile.setExpertiseReviewedBy(null);
        }

        return toResponse(mentorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteMentorProfile(UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        MentorProfile profile = findMentorProfileByUserId(userId);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.NONE);
        user.setIsMentor(false);
        profile.setExpertiseStatus(VerificationStatus.NOT_SUBMITTED);
        userRepository.save(user);
        mentorProfileRepository.delete(profile);
    }

    @Override
    public Page<MentorProfileResponse> getAllApprovedMentors(Pageable pageable) {
        return mentorProfileRepository.findApproved(pageable).map(this::toResponse);
    }

    @Override
    public Page<MentorProfileResponse> getPendingMentorApplications(Pageable pageable) {
        return mentorProfileRepository.findByMentorStatus(MentorStatus.PENDING, pageable).map(this::toResponse);
    }

    @Override
    public Page<MentorProfileResponse> getMentorsWithFilters(BigDecimal minRating, BigDecimal maxHourlyRate,
                                                             String availability, String primaryDomain,
                                                             String skillKeyword, Pageable pageable) {
        return mentorProfileRepository.findApprovedWithAdvancedFilters(
                        minRating,
                        maxHourlyRate,
                        normalizeNullable(availability),
                        normalizeNullable(primaryDomain),
                        normalizeNullable(skillKeyword),
                        pageable
                )
                .map(this::toResponse);
    }

    @Override
    public List<MentorProfileResponse> getFeaturedMentors() {
        return mentorProfileRepository.findFeatured().stream().map(this::toResponse).toList();
    }

    @Override
    public Page<MentorProfileResponse> getTopRatedMentors(Pageable pageable) {
        return mentorProfileRepository.findApproved(pageable).map(this::toResponse);
    }

    @Override
    public List<MentorProfileResponse> searchMentors(String searchQuery) {
        return mentorProfileRepository.search(searchQuery).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MentorProfileResponse approveMentorApplication(UUID userId, UUID approvedBy) {
        mentorModeAccessService.requireMentorApplicationModeration();
        MentorProfile profile = findMentorProfileByUserId(userId);
        User approver = findUser(approvedBy);
        User user = profile.getUser();
        MentorStatus previousStatus = user.getMentorStatus();
        user.setMentorStatus(MentorStatus.APPROVED);
        user.setIsMentor(true);
        userRepository.save(user);
        assignMentorRoleIfMissing(user, approver);

        profile.setExpertiseStatus(VerificationStatus.APPROVED);
        profile.setExpertiseReviewedBy(approver);
        profile.setExpertiseReviewedAt(LocalDateTime.now());
        profile.setExpertiseReviewNote("Approved for Mentor Mode");
        profile.setExpertiseRejectionReason(null);
        profile.setResubmissionAllowed(false);
        profile.setApprovedBy(approver);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(null);
        MentorProfile savedProfile = mentorProfileRepository.save(profile);

        if (previousStatus != MentorStatus.APPROVED) {
            String mentorDashboardUrl = frontendBaseUrl + "/mentor/dashboard";
            String recipientName = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                    ? user.getDisplayName()
                    : user.getFullName();
            emailService.sendMentorApprovalEmail(user.getEmail(), recipientName, mentorDashboardUrl);
            log.info("Queued mentor approval email for user {}", user.getEmail());
        }

        return toResponse(savedProfile);
    }

    @Override
    @Transactional
    public MentorProfileResponse rejectMentorApplication(UUID userId, String rejectionReason, UUID rejectedBy) {
        mentorModeAccessService.requireMentorApplicationModeration();
        MentorProfile profile = findMentorProfileByUserId(userId);
        User rejector = findUser(rejectedBy);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.REJECTED);
        user.setIsMentor(false);
        userRepository.save(user);

        profile.setExpertiseStatus(VerificationStatus.REJECTED);
        profile.setExpertiseReviewedBy(rejector);
        profile.setExpertiseReviewedAt(LocalDateTime.now());
        profile.setExpertiseRejectionReason(rejectionReason);
        profile.setResubmissionAllowed(false);
        profile.setApprovedBy(rejector);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(rejectionReason);
        return toResponse(mentorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public MentorProfileResponse requestMentorApplicationRevision(UUID userId, String revisionReason, UUID requestedBy) {
        mentorModeAccessService.requireMentorApplicationModeration();
        MentorProfile profile = findMentorProfileByUserId(userId);
        User reviewer = findUser(requestedBy);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.PENDING);
        user.setIsMentor(false);
        userRepository.save(user);

        profile.setExpertiseStatus(VerificationStatus.NEEDS_MORE_INFO);
        profile.setExpertiseReviewedBy(reviewer);
        profile.setExpertiseReviewedAt(LocalDateTime.now());
        profile.setExpertiseReviewNote(revisionReason);
        profile.setExpertiseRejectionReason(revisionReason);
        profile.setResubmissionAllowed(true);
        profile.setApprovedBy(reviewer);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(revisionReason);
        MentorProfile savedProfile = mentorProfileRepository.save(profile);

        // Send Email
        String updateUrl = frontendBaseUrl + "/become-a-mentor";
        String recipientName = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : user.getFullName();
        emailService.sendMentorMoreInfoRequestEmail(user.getEmail(), recipientName, revisionReason, updateUrl);

        // Send Notification
        NotificationCreateRequest notificationRequest = new NotificationCreateRequest(
                user.getId(),
                NotificationType.ACCOUNT_VERIFICATION,
                "Mentor Application Update Required",
                "Your mentor application requires more information.",
                profile.getId(),
                "MENTOR_PROFILE",
                updateUrl,
                null,
                1,
                null,
                "MENTOR_APPLICATION",
                null,
                reviewer.getId()
        );
        notificationService.sendNotification(notificationRequest);

        return toResponse(savedProfile);
    }

    @Override
    @Transactional
    public MentorProfileResponse suspendMentor(UUID userId, String suspensionReason, UUID suspendedBy) {
        mentorModeAccessService.requireMentorApplicationModeration();
        MentorProfile profile = findMentorProfileByUserId(userId);
        User moderator = findUser(suspendedBy);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.SUSPENDED);
        user.setIsMentor(false);
        userRepository.save(user);

        profile.setExpertiseReviewNote(suspensionReason);
        profile.setResubmissionAllowed(false);
        profile.setApprovedBy(moderator);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(suspensionReason);
        return toResponse(mentorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void setFeaturedStatus(UUID userId, boolean featured) {
        MentorProfile profile = findMentorProfileByUserId(userId);
        profile.setIsFeatured(featured);
        mentorProfileRepository.save(profile);
    }

    @Override
    public boolean isMentorSaved(UUID userId, UUID mentorUserId) {
        findUser(userId);
        findMentorProfileByUserId(mentorUserId);
        return userSaveRepository.existsByUserIdAndTargetTypeAndTargetId(userId, MENTOR_SAVE_TARGET_TYPE, mentorUserId);
    }

    @Override
    @Transactional
    public boolean saveMentor(UUID userId, UUID mentorUserId) {
        if (userId.equals(mentorUserId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        User user = findUser(userId);
        findMentorProfileByUserId(mentorUserId);

        if (!userSaveRepository.existsByUserIdAndTargetTypeAndTargetId(userId, MENTOR_SAVE_TARGET_TYPE, mentorUserId)) {
            userSaveRepository.save(UserSave.builder()
                    .user(user)
                    .targetType(MENTOR_SAVE_TARGET_TYPE)
                    .targetId(mentorUserId)
                    .build());
        }

        return true;
    }

    @Override
    @Transactional
    public boolean unsaveMentor(UUID userId, UUID mentorUserId) {
        findUser(userId);
        findMentorProfileByUserId(mentorUserId);
        userSaveRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, MENTOR_SAVE_TARGET_TYPE, mentorUserId);
        return false;
    }

    @Override
    public List<MentorProfileResponse> getSavedMentors(UUID userId) {
        findUser(userId);
        return userSaveRepository.findByUserIdAndTargetTypeOrderBySavedAtDesc(userId, MENTOR_SAVE_TARGET_TYPE)
                .stream()
                .map(UserSave::getTargetId)
                .map(this::findMentorProfileByUserId)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public long getApprovedMentorsCount() {
        return mentorProfileRepository.countByMentorStatus(MentorStatus.APPROVED);
    }

    @Override
    public long getPendingApplicationsCount() {
        return mentorProfileRepository.countByMentorStatus(MentorStatus.PENDING);
    }

    @Override
    public UserResponse getCurrentApplicationStatus(UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        return userMapper.toUserResponse(findUser(userId));
    }

    private MentorProfile findMentorProfileByUserId(UUID userId) {
        return mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setUserRoles(userRoleRepository.findByUserIdWithRole(userId));
        return user;
    }

    @Override
    public MentorProfileResponse toResponse(MentorProfile profile) {
        UserBankAccount payoutAccount = userBankAccountRepository.findByUserIdAndIsDefaultTrue(profile.getUser().getId()).orElse(null);
        List<ProofLinkItem> proofLinks = profile.getProofLinks();
        if (proofLinks == null || proofLinks.isEmpty()) {
            proofLinks = buildLegacyProofLinks(profile);
        }
        return new MentorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                userMapper.toUserResponse(profile.getUser()),
                profile.getHeadline(),
                profile.getHourlyRateMxc(),
                profile.getYearsOfExperience(),
                profile.getAvailability(),
                profile.getResponseTimeHours(),
                profile.getTotalJobsDone(),
                profile.getSuccessRate(),
                profile.getAverageRating(),
                profile.getTotalReviews(),
                profile.getIsFeatured(),
                profile.getCvUrl(),
                profile.getPortfolioUrl(),
                profile.getVideoIntroUrl(),
                profile.getLocation(),
                profile.getLanguages(),
                profile.getExpertiseStatus(),
                profile.getExpertiseReviewNote(),
                profile.getExpertiseRejectionReason(),
                profile.getExpertiseReviewedBy() != null ? profile.getExpertiseReviewedBy().getId() : null,
                profile.getExpertiseReviewedBy() != null ? profile.getExpertiseReviewedBy().getFullName() : null,
                profile.getExpertiseReviewedAt(),
                profile.getResubmissionAllowed(),
                profile.getLegalName(),
                profile.getDateOfBirth(),
                profile.getCountryOfResidence(),
                profile.getIdentityStatus(),
                profile.getIdentityRequired(),
                profile.getIdentityDocumentType(),
                profile.getDocumentNumberMasked(),
                profile.getIdentityVerifiedAt(),
                profile.getIdentityVerifiedBy() != null ? profile.getIdentityVerifiedBy().getId() : null,
                profile.getIdentityVerifiedBy() != null ? profile.getIdentityVerifiedBy().getFullName() : null,
                profile.getIdentityRejectionReason(),
                profile.getVerificationProvider(),
                profile.getPhoneNumber(),
                profile.getPhoneVerified(),
                profile.getCurrentTitle(),
                profile.getCurrentCompany(),
                profile.getPrimaryDomain(),
                profile.getSkills(),
                profile.getProfessionalBio(),
                profile.getHelpDescription(),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getPortfolioEvidenceUrl(),
                proofLinks,
                profile.getCertificateUrl(),
                profile.getPayoutStatus(),
                payoutAccount != null ? payoutAccount.getAccountHolderName() : null,
                payoutAccount != null ? payoutAccount.getBankName() : null,
                payoutAccount != null ? maskPayoutReference(payoutAccount.getAccountNumber(), payoutAccount.getIban()) : null,
                profile.getPayoutCountry(),
                profile.getPayoutMethod(),
                profile.getIban(),
                profile.getSwiftCode(),
                profile.getPaypalEmail(),
                profile.getWiseEmail(),
                profile.getStripeConnectAccountId(),
                profile.getPayoutRejectionReason(),
                profile.getPayoutReviewedBy() != null ? profile.getPayoutReviewedBy().getId() : null,
                profile.getPayoutReviewedBy() != null ? profile.getPayoutReviewedBy().getFullName() : null,
                profile.getPayoutReviewedAt(),
                profile.getMentorAgreementAccepted(),
                profile.getDisputePolicyAccepted(),
                profile.getSubmittedAt(),
                profile.getApprovedBy() != null ? profile.getApprovedBy().getId() : null,
                profile.getApprovedBy() != null ? profile.getApprovedBy().getFullName() : null,
                profile.getApprovedAt(),
                profile.getRejectionReason(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private String maskPayoutReference(String accountNumber, String iban) {
        String raw = accountNumber != null && !accountNumber.isBlank() ? accountNumber : iban;
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() <= 4) {
            return trimmed;
        }
        return "*".repeat(Math.max(0, trimmed.length() - 4)) + trimmed.substring(trimmed.length() - 4);
    }

    private void applyProfessionalFields(MentorProfile profile, MentorProfileRequest request, boolean patchOnly) {
        if (!patchOnly || request.currentTitle() != null) profile.setCurrentTitle(request.currentTitle());
        if (!patchOnly || request.currentCompany() != null) profile.setCurrentCompany(request.currentCompany());
        if (!patchOnly || request.primaryDomain() != null) profile.setPrimaryDomain(request.primaryDomain());
        if (!patchOnly || request.skills() != null) profile.setSkills(normalizeSkills(request.skills()));
        if (!patchOnly || request.professionalBio() != null) profile.setProfessionalBio(request.professionalBio());
        if (!patchOnly || request.helpDescription() != null) profile.setHelpDescription(request.helpDescription());
        if (!patchOnly || request.linkedinUrl() != null) profile.setLinkedinUrl(request.linkedinUrl());
        if (!patchOnly || request.githubUrl() != null) profile.setGithubUrl(request.githubUrl());
        if (!patchOnly || request.portfolioEvidenceUrl() != null) profile.setPortfolioEvidenceUrl(request.portfolioEvidenceUrl());
        if (!patchOnly || request.proofLinks() != null) profile.setProofLinks(normalizeProofLinks(request.proofLinks()));
        if (!patchOnly || request.certificateUrl() != null) profile.setCertificateUrl(request.certificateUrl());
        if (!patchOnly || request.mentorAgreementAccepted() != null) profile.setMentorAgreementAccepted(Boolean.TRUE.equals(request.mentorAgreementAccepted()));
        if (!patchOnly || request.disputePolicyAccepted() != null) profile.setDisputePolicyAccepted(Boolean.TRUE.equals(request.disputePolicyAccepted()));

        if ((profile.getProofLinks() == null || profile.getProofLinks().isEmpty()) && hasAnyLegacyProofLink(profile)) {
            profile.setProofLinks(buildLegacyProofLinks(profile));
        }

        if (Boolean.TRUE.equals(profile.getMentorAgreementAccepted())
                && Boolean.TRUE.equals(profile.getDisputePolicyAccepted())
                && profile.getSubmittedAt() == null) {
            profile.setSubmittedAt(LocalDateTime.now());
        }

        if (Boolean.TRUE.equals(profile.getMentorAgreementAccepted())
                && Boolean.TRUE.equals(profile.getDisputePolicyAccepted())
                && (profile.getExpertiseStatus() == VerificationStatus.NOT_SUBMITTED
                || profile.getExpertiseStatus() == VerificationStatus.NEEDS_MORE_INFO)) {
            profile.setExpertiseStatus(VerificationStatus.PENDING);
            profile.setResubmissionAllowed(false);
            profile.setExpertiseReviewNote(null);
            profile.setExpertiseRejectionReason(null);
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return null;
        }
        return skills.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void validateMentorApplicationPayload(MentorProfile profile) {
        requireNonBlank(profile.getHeadline(), "Headline is required.");
        requireNonBlank(profile.getPrimaryDomain(), "Primary domain is required.");
        if (profile.getSkills() == null || profile.getSkills().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "At least one skill is required.");
        }
        requireLength(profile.getProfessionalBio(), 50, 500, "Professional bio must be 50 to 500 characters.");
        requireLength(profile.getHelpDescription(), 30, 500, "Help description must be 30 to 500 characters.");
        if (profile.getYearsOfExperience() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Years of experience is required.");
        }
        requireNonBlank(profile.getAvailability(), "Availability is required.");
        requireNonBlank(profile.getLocation(), "Location / timezone is required.");
        if (profile.getLanguages() == null || profile.getLanguages().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "At least one language is required.");
        }
        if (!Boolean.TRUE.equals(profile.getMentorAgreementAccepted())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "You must confirm your professional information accuracy.");
        }
        if (!Boolean.TRUE.equals(profile.getDisputePolicyAccepted())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "You must agree to moderation and dispute policies.");
        }

        validateTextFieldNotUrl(profile.getHeadline(), "Headline");
        validateTextFieldNotUrl(profile.getCurrentTitle(), "Current title");
        validateTextFieldNotUrl(profile.getCurrentCompany(), "Current company");

        validateUrl(profile.getLinkedinUrl(), "LinkedIn profile", "linkedin.com");
        validateUrl(profile.getGithubUrl(), "GitHub profile", "github.com");
        validateUrl(profile.getPortfolioUrl(), "Portfolio", null);
        validateUrl(profile.getPortfolioEvidenceUrl(), "Proof of work", null);
        validateProofLinks(profile.getProofLinks());

        if (!hasAnyProof(profile)) {
            throw new AppException(
                    ErrorCode.BAD_REQUEST,
                    "At least one proof item is required: add a proof link, CV, certificate, or portfolio evidence."
            );
        }
    }

    private void requireNonBlank(String value, String message) {
        if (isBlank(value)) {
            throw new AppException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private void requireLength(String value, int min, int max, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new AppException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateTextFieldNotUrl(String value, String fieldName) {
        if (isBlank(value)) return;
        String lower = value.trim().toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.")) {
            throw new AppException(ErrorCode.BAD_REQUEST, fieldName + " must be plain text, not a URL.");
        }
    }

    private void validateUrl(String value, String fieldName, String requiredHost) {
        if (isBlank(value)) return;
        URI uri;
        try {
            uri = new URI(value.trim());
        } catch (URISyntaxException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, fieldName + " is not a valid URL.");
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new AppException(ErrorCode.BAD_REQUEST, fieldName + " must be a valid http/https URL.");
        }

        String hostLower = host.toLowerCase(Locale.ROOT);
        if (!isDevelopmentProfile() && (hostLower.equals("localhost") || hostLower.equals("127.0.0.1") || hostLower.endsWith(".local"))) {
            throw new AppException(ErrorCode.BAD_REQUEST, fieldName + " cannot use localhost URL outside development.");
        }

        if (requiredHost != null && !(hostLower.equals(requiredHost) || hostLower.endsWith("." + requiredHost))) {
            throw new AppException(ErrorCode.BAD_REQUEST, fieldName + " must use " + requiredHost + ".");
        }
    }

    private List<ProofLinkItem> normalizeProofLinks(List<ProofLinkItem> proofLinks) {
        if (proofLinks == null) {
            return null;
        }

        List<ProofLinkItem> normalized = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (ProofLinkItem item : proofLinks) {
            if (item == null) {
                continue;
            }

            String label = normalizeNullable(item.getLabel());
            String url = normalizeNullable(item.getUrl());
            if (label == null && url == null) {
                continue;
            }

            String key = (label == null ? "" : label.toLowerCase(Locale.ROOT))
                    + "|"
                    + (url == null ? "" : url.toLowerCase(Locale.ROOT));
            if (seen.add(key)) {
                normalized.add(new ProofLinkItem(label, url));
            }
        }

        return normalized;
    }

    private void validateProofLinks(List<ProofLinkItem> proofLinks) {
        if (proofLinks == null || proofLinks.isEmpty()) {
            return;
        }

        for (int index = 0; index < proofLinks.size(); index++) {
            ProofLinkItem item = proofLinks.get(index);
            if (item == null) {
                continue;
            }

            String label = normalizeNullable(item.getLabel());
            String url = normalizeNullable(item.getUrl());
            String prefix = "Proof link #" + (index + 1);

            if (label == null || url == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, prefix + " must include both a label and a URL.");
            }

            if (label.length() > 80) {
                throw new AppException(ErrorCode.BAD_REQUEST, prefix + " label must not exceed 80 characters.");
            }

            validateUrl(url, prefix, null);
        }
    }

    private boolean hasAnyProof(MentorProfile profile) {
        return (profile.getProofLinks() != null && !profile.getProofLinks().isEmpty())
                || !isBlank(profile.getLinkedinUrl())
                || !isBlank(profile.getGithubUrl())
                || !isBlank(profile.getPortfolioUrl())
                || !isBlank(profile.getCvUrl())
                || !isBlank(profile.getCertificateUrl())
                || !isBlank(profile.getPortfolioEvidenceUrl());
    }

    private boolean hasAnyLegacyProofLink(MentorProfile profile) {
        return !isBlank(profile.getLinkedinUrl())
                || !isBlank(profile.getGithubUrl())
                || !isBlank(profile.getPortfolioUrl())
                || !isBlank(profile.getPortfolioEvidenceUrl())
                || !isBlank(profile.getVideoIntroUrl());
    }

    private List<ProofLinkItem> buildLegacyProofLinks(MentorProfile profile) {
        List<ProofLinkItem> links = new ArrayList<>();
        addProofLink(links, "LinkedIn profile", profile.getLinkedinUrl());
        addProofLink(links, "GitHub profile", profile.getGithubUrl());
        addProofLink(links, "Portfolio", profile.getPortfolioUrl());
        addProofLink(links, "Proof of work", profile.getPortfolioEvidenceUrl());
        addProofLink(links, "Intro video", profile.getVideoIntroUrl());
        return links;
    }

    private void addProofLink(List<ProofLinkItem> links, String label, String url) {
        String normalizedUrl = normalizeNullable(url);
        if (normalizedUrl == null) {
            return;
        }
        links.add(new ProofLinkItem(label, normalizedUrl));
    }

    private boolean isDevelopmentProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> "dev".equalsIgnoreCase(profile));
    }

    private boolean canEditSubmittedProfile(MentorProfile profile) {
        VerificationStatus expertiseStatus = profile.getExpertiseStatus();
        if (expertiseStatus == VerificationStatus.NOT_SUBMITTED || expertiseStatus == VerificationStatus.APPROVED) {
            return true;
        }
        return expertiseStatus == VerificationStatus.NEEDS_MORE_INFO
                && Boolean.TRUE.equals(profile.getResubmissionAllowed());
    }

    private void assignMentorRoleIfMissing(User user, User approver) {
        roleRepository.findByRoleName("MENTOR").ifPresent(role -> {
            if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
                userRoleRepository.save(UserRole.builder()
                        .userId(user.getId())
                        .roleId(role.getId())
                        .user(user)
                        .role(role)
                        .grantedBy(approver)
                        .grantedAt(LocalDateTime.now())
                        .build());
            }
        });
    }
}

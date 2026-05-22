package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
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
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserBankAccountRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.user.service.MentorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    public Page<MentorProfileResponse> getMentorsWithFilters(BigDecimal minRating, BigDecimal maxHourlyRate, String availability, Pageable pageable) {
        return mentorProfileRepository.findApprovedWithFilters(minRating, maxHourlyRate, availability, pageable)
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
        user.setMentorStatus(MentorStatus.APPROVED);
        user.setIsMentor(true);
        userRepository.save(user);
        assignMentorRoleIfMissing(user, approver);

        profile.setExpertiseStatus(VerificationStatus.APPROVED);
        profile.setExpertiseReviewedBy(approver);
        profile.setExpertiseReviewedAt(LocalDateTime.now());
        profile.setExpertiseReviewNote("Approved for Mentor Mode");
        profile.setExpertiseRejectionReason(null);
        profile.setApprovedBy(approver);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(null);
        return toResponse(mentorProfileRepository.save(profile));
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
        profile.setApprovedBy(reviewer);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(revisionReason);
        return toResponse(mentorProfileRepository.save(profile));
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
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getPortfolioEvidenceUrl(),
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
        if (!patchOnly || request.linkedinUrl() != null) profile.setLinkedinUrl(request.linkedinUrl());
        if (!patchOnly || request.githubUrl() != null) profile.setGithubUrl(request.githubUrl());
        if (!patchOnly || request.portfolioEvidenceUrl() != null) profile.setPortfolioEvidenceUrl(request.portfolioEvidenceUrl());
        if (!patchOnly || request.certificateUrl() != null) profile.setCertificateUrl(request.certificateUrl());
        if (!patchOnly || request.mentorAgreementAccepted() != null) profile.setMentorAgreementAccepted(Boolean.TRUE.equals(request.mentorAgreementAccepted()));
        if (!patchOnly || request.disputePolicyAccepted() != null) profile.setDisputePolicyAccepted(Boolean.TRUE.equals(request.disputePolicyAccepted()));

        if (Boolean.TRUE.equals(profile.getMentorAgreementAccepted())
                && Boolean.TRUE.equals(profile.getDisputePolicyAccepted())
                && profile.getSubmittedAt() == null) {
            profile.setSubmittedAt(LocalDateTime.now());
        }

        if (Boolean.TRUE.equals(profile.getMentorAgreementAccepted())
                && Boolean.TRUE.equals(profile.getDisputePolicyAccepted())
                && profile.getExpertiseStatus() == VerificationStatus.NOT_SUBMITTED) {
            profile.setExpertiseStatus(VerificationStatus.PENDING);
        }
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

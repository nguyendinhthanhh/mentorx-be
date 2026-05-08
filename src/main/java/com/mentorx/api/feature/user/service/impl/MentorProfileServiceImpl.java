package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.dto.request.MentorProfileRequest;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
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

    @Override
    @Transactional
    public MentorProfileResponse createMentorProfile(UUID userId, MentorProfileRequest request) {
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
                .responseTimeHours(request.responseTimeHours())
                .cvUrl(request.cvUrl())
                .portfolioUrl(request.portfolioUrl())
                .build();

        user.setMentorStatus(MentorStatus.PENDING);
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
        MentorProfile profile = findMentorProfileByUserId(userId);

        if (request.headline() != null) profile.setHeadline(request.headline());
        if (request.hourlyRateMxc() != null) profile.setHourlyRateMxc(request.hourlyRateMxc());
        if (request.yearsOfExperience() != null) profile.setYearsOfExperience(request.yearsOfExperience());
        if (request.availability() != null) profile.setAvailability(request.availability());
        if (request.responseTimeHours() != null) profile.setResponseTimeHours(request.responseTimeHours());
        if (request.cvUrl() != null) profile.setCvUrl(request.cvUrl());
        if (request.portfolioUrl() != null) profile.setPortfolioUrl(request.portfolioUrl());

        return toResponse(mentorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteMentorProfile(UUID userId) {
        MentorProfile profile = findMentorProfileByUserId(userId);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.NONE);
        user.setIsMentor(false);
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
        MentorProfile profile = findMentorProfileByUserId(userId);
        User approver = findUser(approvedBy);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.APPROVED);
        user.setIsMentor(true);
        userRepository.save(user);

        profile.setApprovedBy(approver);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(null);
        return toResponse(mentorProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public MentorProfileResponse rejectMentorApplication(UUID userId, String rejectionReason, UUID rejectedBy) {
        MentorProfile profile = findMentorProfileByUserId(userId);
        User rejector = findUser(rejectedBy);
        User user = profile.getUser();
        user.setMentorStatus(MentorStatus.REJECTED);
        user.setIsMentor(false);
        userRepository.save(user);

        profile.setApprovedBy(rejector);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setRejectionReason(rejectionReason);
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
    public long getApprovedMentorsCount() {
        return mentorProfileRepository.countByMentorStatus(MentorStatus.APPROVED);
    }

    @Override
    public long getPendingApplicationsCount() {
        return mentorProfileRepository.countByMentorStatus(MentorStatus.PENDING);
    }

    private MentorProfile findMentorProfileByUserId(UUID userId) {
        return mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private MentorProfileResponse toResponse(MentorProfile profile) {
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
                profile.getApprovedBy() != null ? profile.getApprovedBy().getId() : null,
                profile.getApprovedBy() != null ? profile.getApprovedBy().getFullName() : null,
                profile.getApprovedAt(),
                profile.getRejectionReason(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}

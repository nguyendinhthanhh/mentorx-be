package com.mentorx.api.feature.mentor.service.impl;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.mentor.dto.request.MentorApplicationRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.mentor.entity.MentorProfile;
import com.mentorx.api.feature.mentor.mapper.MentorMapper;
import com.mentorx.api.feature.mentor.repository.MentorProfileRepository;
import com.mentorx.api.feature.mentor.repository.MentorSkillRepository;
import com.mentorx.api.feature.mentor.service.MentorService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
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
public class MentorServiceImpl implements MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorSkillRepository mentorSkillRepository;
    private final UserRepository userRepository;
    private final MentorMapper mentorMapper;

    @Override
    @Transactional
    public MentorProfileResponse applyAsMentor(UUID userId, MentorApplicationRequest request) {
        log.info("Processing mentor application for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if user already has a mentor profile
        if (mentorProfileRepository.findByUserId(userId).isPresent()) {
            throw new AppException(ErrorCode.MENTOR_APPLICATION_ALREADY_EXISTS);
        }

        MentorProfile mentorProfile = mentorMapper.toMentorProfile(request);
        mentorProfile.setUser(user);
        mentorProfile.setStatus(MentorStatus.PENDING);

        MentorProfile savedProfile = mentorProfileRepository.save(mentorProfile);
        log.info("Mentor application submitted for user: {}", userId);

        return mentorMapper.toMentorProfileResponse(savedProfile);
    }

    @Override
    public MentorProfileResponse getMentorProfile(UUID mentorProfileId) {
        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);
        return mentorMapper.toMentorProfileResponse(mentorProfile);
    }

    @Override
    public MentorProfileResponse getMentorProfileByUserId(UUID userId) {
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        return mentorMapper.toMentorProfileResponse(mentorProfile);
    }

    @Override
    @Transactional
    public MentorProfileResponse updateMentorProfile(UUID mentorProfileId, MentorApplicationRequest request) {
        log.info("Updating mentor profile: {}", mentorProfileId);

        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);

        // Only allow updates if status is PENDING or APPROVED
        if (mentorProfile.getStatus() == MentorStatus.REJECTED) {
            throw new AppException(ErrorCode.MENTOR_PROFILE_CANNOT_BE_UPDATED);
        }

        mentorMapper.updateMentorProfile(mentorProfile, request);
        MentorProfile updatedProfile = mentorProfileRepository.save(mentorProfile);

        log.info("Mentor profile updated: {}", mentorProfileId);
        return mentorMapper.toMentorProfileResponse(updatedProfile);
    }

    @Override
    @Transactional
    public void deleteMentorProfile(UUID mentorProfileId) {
        log.info("Deleting mentor profile: {}", mentorProfileId);
        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);
        mentorProfileRepository.delete(mentorProfile);
        log.info("Mentor profile deleted: {}", mentorProfileId);
    }

    @Override
    @Transactional
    public MentorProfileResponse approveMentorApplication(UUID mentorProfileId, UUID approvedBy, String adminNotes) {
        log.info("Approving mentor application: {} by {}", mentorProfileId, approvedBy);

        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);
        User approver = userRepository.findById(approvedBy)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        mentorProfile.setStatus(MentorStatus.APPROVED);
        mentorProfile.setApprovedBy(approver);
        mentorProfile.setApprovedAt(LocalDateTime.now());
        mentorProfile.setAdminNotes(adminNotes);

        // Update user mentor status
        User user = mentorProfile.getUser();
        user.setIsMentor(true);
        user.setMentorStatus(MentorStatus.APPROVED);
        userRepository.save(user);

        MentorProfile updatedProfile = mentorProfileRepository.save(mentorProfile);
        log.info("Mentor application approved: {}", mentorProfileId);

        return mentorMapper.toMentorProfileResponse(updatedProfile);
    }

    @Override
    @Transactional
    public MentorProfileResponse rejectMentorApplication(UUID mentorProfileId, UUID rejectedBy, String adminNotes) {
        log.info("Rejecting mentor application: {} by {}", mentorProfileId, rejectedBy);

        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);

        mentorProfile.setStatus(MentorStatus.REJECTED);
        mentorProfile.setRejectedAt(LocalDateTime.now());
        mentorProfile.setAdminNotes(adminNotes);

        // Update user mentor status
        User user = mentorProfile.getUser();
        user.setMentorStatus(MentorStatus.REJECTED);
        userRepository.save(user);

        MentorProfile updatedProfile = mentorProfileRepository.save(mentorProfile);
        log.info("Mentor application rejected: {}", mentorProfileId);

        return mentorMapper.toMentorProfileResponse(updatedProfile);
    }

    @Override
    @Transactional
    public MentorProfileResponse updateMentorStatus(UUID mentorProfileId, MentorStatus status) {
        log.info("Updating mentor status: {} -> {}", mentorProfileId, status);

        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);
        mentorProfile.setStatus(status);

        // Update user mentor status
        User user = mentorProfile.getUser();
        user.setMentorStatus(status);
        user.setIsMentor(status == MentorStatus.APPROVED);
        userRepository.save(user);

        MentorProfile updatedProfile = mentorProfileRepository.save(mentorProfile);
        log.info("Mentor status updated: {} -> {}", mentorProfileId, status);

        return mentorMapper.toMentorProfileResponse(updatedProfile);
    }

    @Override
    public Page<MentorProfileResponse> getAllMentors(Pageable pageable) {
        return mentorProfileRepository.findAll(pageable)
                .map(mentorMapper::toMentorProfileResponse);
    }

    @Override
    public Page<MentorProfileResponse> getMentorsByStatus(MentorStatus status, Pageable pageable) {
        return mentorProfileRepository.findByStatus(status, pageable)
                .map(mentorMapper::toMentorProfileResponse);
    }

    @Override
    public Page<MentorProfileResponse> getAvailableMentorsForJobs(Pageable pageable) {
        return mentorProfileRepository.findAvailableMentorsForJobs(MentorStatus.APPROVED, pageable)
                .map(mentorMapper::toMentorProfileResponse);
    }

    @Override
    public Page<MentorProfileResponse> getAvailableMentorsForCourses(Pageable pageable) {
        return mentorProfileRepository.findAvailableMentorsForCourses(MentorStatus.APPROVED, pageable)
                .map(mentorMapper::toMentorProfileResponse);
    }

    @Override
    public List<MentorProfileResponse> searchMentorsBySkill(String skillName) {
        List<MentorProfile> mentors = mentorProfileRepository.findBySkillName(skillName);
        return mentors.stream()
                .map(mentorMapper::toMentorProfileResponse)
                .toList();
    }

    @Override
    public List<MentorProfileResponse> getMentorsByHourlyRateRange(BigDecimal minRate, BigDecimal maxRate) {
        List<MentorProfile> mentors = mentorProfileRepository.findByHourlyRateRange(minRate, maxRate);
        return mentors.stream()
                .map(mentorMapper::toMentorProfileResponse)
                .toList();
    }

    @Override
    public Page<MentorProfileResponse> getMentorsWithFilters(String skillName, BigDecimal minRate, BigDecimal maxRate,
                                                           Integer minExperience, Boolean availableForJobs, Pageable pageable) {
        return mentorProfileRepository.findMentorsWithFilters(skillName, minRate, maxRate, minExperience, availableForJobs, pageable)
                .map(mentorMapper::toMentorProfileResponse);
    }

    @Override
    public List<MentorProfileResponse> searchMentors(String searchTerm) {
        List<MentorProfile> mentors = mentorProfileRepository.findByFullTextSearch(searchTerm);
        return mentors.stream()
                .map(mentorMapper::toMentorProfileResponse)
                .toList();
    }

    @Override
    public List<MentorProfileResponse> getMentorsByMaxResponseTime(Integer maxResponseTime) {
        List<MentorProfile> mentors = mentorProfileRepository.findByMaxResponseTime(maxResponseTime);
        return mentors.stream()
                .map(mentorMapper::toMentorProfileResponse)
                .toList();
    }

    @Override
    public long getTotalMentorsCount() {
        return mentorProfileRepository.count();
    }

    @Override
    public long getApprovedMentorsCount() {
        return mentorProfileRepository.countByStatus(MentorStatus.APPROVED);
    }

    @Override
    public long getPendingApplicationsCount() {
        return mentorProfileRepository.countByStatus(MentorStatus.PENDING);
    }

    @Override
    public List<String> getPopularSkills() {
        return mentorSkillRepository.findSkillPopularity().stream()
                .map(result -> (String) result[0])
                .toList();
    }

    @Override
    public List<String> searchSkills(String skillName) {
        return mentorSkillRepository.findDistinctSkillNamesByPattern(skillName);
    }

    @Override
    @Transactional
    public void updateMentorAvailability(UUID mentorProfileId, Boolean availableForJobs, Boolean availableForCourses) {
        log.info("Updating mentor availability: {}", mentorProfileId);

        MentorProfile mentorProfile = findMentorProfileById(mentorProfileId);

        if (availableForJobs != null) {
            mentorProfile.setIsAvailableForJobs(availableForJobs);
        }
        if (availableForCourses != null) {
            mentorProfile.setIsAvailableForCourses(availableForCourses);
        }

        mentorProfileRepository.save(mentorProfile);
        log.info("Mentor availability updated: {}", mentorProfileId);
    }

    private MentorProfile findMentorProfileById(UUID mentorProfileId) {
        return mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
    }
}
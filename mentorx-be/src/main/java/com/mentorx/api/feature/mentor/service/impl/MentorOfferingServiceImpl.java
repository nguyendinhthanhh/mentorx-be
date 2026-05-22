package com.mentorx.api.feature.mentor.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.mentor.dto.request.MentorOfferingRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorOfferingResponse;
import com.mentorx.api.feature.mentor.entity.MentorOffering;
import com.mentorx.api.feature.mentor.repository.MentorOfferingRepository;
import com.mentorx.api.feature.mentor.service.MentorOfferingService;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorOfferingServiceImpl implements MentorOfferingService {

    private final MentorOfferingRepository mentorOfferingRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public MentorOfferingResponse createCourse(UUID userId, MentorOfferingRequest request) {
        log.info("Creating course for user: {}", userId);
        mentorModeAccessService.requireApprovedMentorContentAccess(userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        MentorOffering mentorOffering = new MentorOffering();
        mentorOffering.setMentorProfileId(mentorProfile.getId());
        mentorOffering.setTitle(request.getTitle());
        mentorOffering.setDescription(request.getDescription());
        mentorOffering.setPriceMxc(request.getPriceMxc());
        mentorOffering.setDurationHours(request.getDurationHours());
        mentorOffering.setLevel(request.getLevel());
        mentorOffering.setLessonsCount(request.getLessonsCount());
        mentorOffering.setThumbnailUrl(request.getThumbnailUrl());
        mentorOffering.setStatus(CourseStatus.DRAFT);

        MentorOffering saved = mentorOfferingRepository.save(mentorOffering);
        log.info("Course created successfully with ID: {}", saved.getId());

        return MentorOfferingResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MentorOfferingResponse updateCourse(UUID courseId, MentorOfferingRequest request) {
        log.info("Updating course: {}", courseId);

        MentorOffering mentorOffering = mentorOfferingRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorOffering.getMentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(mentorProfile.getUser().getId());

        mentorOffering.setTitle(request.getTitle());
        mentorOffering.setDescription(request.getDescription());
        mentorOffering.setPriceMxc(request.getPriceMxc());
        mentorOffering.setDurationHours(request.getDurationHours());
        mentorOffering.setLevel(request.getLevel());
        mentorOffering.setLessonsCount(request.getLessonsCount());
        mentorOffering.setThumbnailUrl(request.getThumbnailUrl());

        MentorOffering updated = mentorOfferingRepository.save(mentorOffering);
        log.info("Course updated successfully: {}", courseId);

        return MentorOfferingResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        log.info("Deleting course: {}", courseId);

        MentorOffering mentorOffering = mentorOfferingRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorOffering.getMentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(mentorProfile.getUser().getId());
        mentorOfferingRepository.delete(mentorOffering);
        log.info("Course deleted successfully: {}", courseId);
    }

    @Override
    @Transactional
    public MentorOfferingResponse publishCourse(UUID courseId) {
        log.info("Publishing course: {}", courseId);

        MentorOffering mentorOffering = mentorOfferingRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorOffering.getMentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(mentorProfile.getUser().getId());

        mentorOffering.setStatus(CourseStatus.PUBLISHED);
        MentorOffering updated = mentorOfferingRepository.save(mentorOffering);

        log.info("Course published successfully: {}", courseId);
        return MentorOfferingResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public MentorOfferingResponse archiveCourse(UUID courseId) {
        log.info("Archiving course: {}", courseId);

        MentorOffering mentorOffering = mentorOfferingRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorOffering.getMentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(mentorProfile.getUser().getId());

        mentorOffering.setStatus(CourseStatus.ARCHIVED);
        MentorOffering updated = mentorOfferingRepository.save(mentorOffering);

        log.info("Course archived successfully: {}", courseId);
        return MentorOfferingResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorOfferingResponse getCourseById(UUID courseId) {
        log.info("Getting course by ID: {}", courseId);

        MentorOffering mentorOffering = mentorOfferingRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        return MentorOfferingResponse.fromEntity(mentorOffering);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorOfferingResponse> getAllCoursesByMentor(UUID userId) {
        log.info("Getting all courses for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        List<MentorOffering> courses = mentorOfferingRepository
                .findByMentorProfileIdOrderByCreatedAtDesc(mentorProfile.getId());

        return courses.stream()
                .map(MentorOfferingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorOfferingResponse> getPublishedCoursesByMentor(UUID userId) {
        log.info("Getting published courses for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        List<MentorOffering> courses = mentorOfferingRepository
                .findByMentorProfileIdAndStatusOrderByCreatedAtDesc(mentorProfile.getId(), CourseStatus.PUBLISHED);

        return courses.stream()
                .map(MentorOfferingResponse::fromEntity)
                .collect(Collectors.toList());
    }
}


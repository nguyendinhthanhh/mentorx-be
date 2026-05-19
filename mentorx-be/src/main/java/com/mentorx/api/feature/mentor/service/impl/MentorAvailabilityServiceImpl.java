package com.mentorx.api.feature.mentor.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.mentor.dto.request.MentorAvailabilityRequest;
import com.mentorx.api.feature.mentor.dto.request.MentorBlockedDateRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorAvailabilityResponse;
import com.mentorx.api.feature.mentor.dto.response.MentorBlockedDateResponse;
import com.mentorx.api.feature.mentor.dto.response.WeeklyAvailabilityResponse;
import com.mentorx.api.feature.mentor.entity.MentorAvailability;
import com.mentorx.api.feature.mentor.entity.MentorBlockedDate;
import com.mentorx.api.feature.mentor.repository.MentorAvailabilityRepository;
import com.mentorx.api.feature.mentor.repository.MentorBlockedDateRepository;
import com.mentorx.api.feature.mentor.service.MentorAvailabilityService;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorAvailabilityServiceImpl implements MentorAvailabilityService {

    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final MentorBlockedDateRepository mentorBlockedDateRepository;
    private final MentorProfileRepository mentorProfileRepository;

    @Override
    @Transactional
    public MentorAvailabilityResponse createAvailability(UUID userId, MentorAvailabilityRequest request) {
        log.info("Creating availability for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        // Validate time range
        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }

        // Check for overlapping slots
        boolean hasOverlap = mentorAvailabilityRepository.existsOverlappingSlot(
                mentorProfile.getId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                UUID.randomUUID() // New record, so use random UUID
        );

        if (hasOverlap) {
            throw new AppException(ErrorCode.AVAILABILITY_OVERLAP);
        }

        MentorAvailability availability = new MentorAvailability();
        availability.setMentorProfileId(mentorProfile.getId());
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setIsActive(request.getIsActive());

        MentorAvailability saved = mentorAvailabilityRepository.save(availability);
        log.info("Availability created successfully with ID: {}", saved.getId());

        return MentorAvailabilityResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MentorAvailabilityResponse updateAvailability(UUID availabilityId, MentorAvailabilityRequest request) {
        log.info("Updating availability: {}", availabilityId);

        MentorAvailability availability = mentorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AppException(ErrorCode.AVAILABILITY_NOT_FOUND));

        // Validate time range
        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }

        // Check for overlapping slots (excluding current record)
        boolean hasOverlap = mentorAvailabilityRepository.existsOverlappingSlot(
                availability.getMentorProfileId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                availabilityId
        );

        if (hasOverlap) {
            throw new AppException(ErrorCode.AVAILABILITY_OVERLAP);
        }

        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setIsActive(request.getIsActive());

        MentorAvailability updated = mentorAvailabilityRepository.save(availability);
        log.info("Availability updated successfully: {}", availabilityId);

        return MentorAvailabilityResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteAvailability(UUID availabilityId) {
        log.info("Deleting availability: {}", availabilityId);

        if (!mentorAvailabilityRepository.existsById(availabilityId)) {
            throw new AppException(ErrorCode.AVAILABILITY_NOT_FOUND);
        }

        mentorAvailabilityRepository.deleteById(availabilityId);
        log.info("Availability deleted successfully: {}", availabilityId);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorAvailabilityResponse getAvailabilityById(UUID availabilityId) {
        log.info("Getting availability by ID: {}", availabilityId);

        MentorAvailability availability = mentorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AppException(ErrorCode.AVAILABILITY_NOT_FOUND));

        return MentorAvailabilityResponse.fromEntity(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorAvailabilityResponse> getAllAvailabilityByMentor(UUID userId) {
        log.info("Getting all availability for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        List<MentorAvailability> availabilities = mentorAvailabilityRepository
                .findByMentorProfileIdOrderByDayOfWeekAscStartTimeAsc(mentorProfile.getId());

        return availabilities.stream()
                .map(MentorAvailabilityResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyAvailabilityResponse getWeeklyAvailability(UUID userId) {
        log.info("Getting weekly availability for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        // Get active availability slots
        List<MentorAvailability> availabilities = mentorAvailabilityRepository
                .findByMentorProfileIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(mentorProfile.getId());

        // Group by day of week
        Map<Integer, List<WeeklyAvailabilityResponse.TimeSlot>> weeklySchedule = availabilities.stream()
                .collect(Collectors.groupingBy(
                        MentorAvailability::getDayOfWeek,
                        Collectors.mapping(
                                a -> new WeeklyAvailabilityResponse.TimeSlot(a.getStartTime(), a.getEndTime()),
                                Collectors.toList()
                        )
                ));

        // Get blocked dates
        List<MentorBlockedDate> blockedDates = mentorBlockedDateRepository
                .findByMentorProfileIdOrderByBlockedDateAsc(mentorProfile.getId());

        List<LocalDate> blockedDatesList = blockedDates.stream()
                .map(MentorBlockedDate::getBlockedDate)
                .collect(Collectors.toList());

        return new WeeklyAvailabilityResponse(weeklySchedule, blockedDatesList);
    }

    @Override
    @Transactional
    public MentorBlockedDateResponse blockDate(UUID userId, MentorBlockedDateRequest request) {
        log.info("Blocking date for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        // Check if date is already blocked
        if (mentorBlockedDateRepository.existsByMentorProfileIdAndBlockedDate(
                mentorProfile.getId(), request.getBlockedDate())) {
            throw new AppException(ErrorCode.DATE_ALREADY_BLOCKED);
        }

        MentorBlockedDate blockedDate = new MentorBlockedDate();
        blockedDate.setMentorProfileId(mentorProfile.getId());
        blockedDate.setBlockedDate(request.getBlockedDate());
        blockedDate.setReason(request.getReason());

        MentorBlockedDate saved = mentorBlockedDateRepository.save(blockedDate);
        log.info("Date blocked successfully: {}", request.getBlockedDate());

        return MentorBlockedDateResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void unblockDate(UUID blockedDateId) {
        log.info("Unblocking date: {}", blockedDateId);

        if (!mentorBlockedDateRepository.existsById(blockedDateId)) {
            throw new AppException(ErrorCode.BLOCKED_DATE_NOT_FOUND);
        }

        mentorBlockedDateRepository.deleteById(blockedDateId);
        log.info("Date unblocked successfully: {}", blockedDateId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorBlockedDateResponse> getBlockedDatesByMentor(UUID userId) {
        log.info("Getting blocked dates for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        List<MentorBlockedDate> blockedDates = mentorBlockedDateRepository
                .findByMentorProfileIdOrderByBlockedDateAsc(mentorProfile.getId());

        return blockedDates.stream()
                .map(MentorBlockedDateResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

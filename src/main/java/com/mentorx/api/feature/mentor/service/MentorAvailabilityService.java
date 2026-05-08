package com.mentorx.api.feature.mentor.service;

import com.mentorx.api.feature.mentor.dto.request.MentorAvailabilityRequest;
import com.mentorx.api.feature.mentor.dto.request.MentorBlockedDateRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorAvailabilityResponse;
import com.mentorx.api.feature.mentor.dto.response.MentorBlockedDateResponse;
import com.mentorx.api.feature.mentor.dto.response.WeeklyAvailabilityResponse;

import java.util.List;
import java.util.UUID;

public interface MentorAvailabilityService {

    MentorAvailabilityResponse createAvailability(UUID userId, MentorAvailabilityRequest request);

    MentorAvailabilityResponse updateAvailability(UUID availabilityId, MentorAvailabilityRequest request);

    void deleteAvailability(UUID availabilityId);

    MentorAvailabilityResponse getAvailabilityById(UUID availabilityId);

    List<MentorAvailabilityResponse> getAllAvailabilityByMentor(UUID userId);

    WeeklyAvailabilityResponse getWeeklyAvailability(UUID userId);

    MentorBlockedDateResponse blockDate(UUID userId, MentorBlockedDateRequest request);

    void unblockDate(UUID blockedDateId);

    List<MentorBlockedDateResponse> getBlockedDatesByMentor(UUID userId);
}

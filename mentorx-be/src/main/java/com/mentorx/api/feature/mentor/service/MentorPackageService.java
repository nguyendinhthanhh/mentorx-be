package com.mentorx.api.feature.mentor.service;

import com.mentorx.api.feature.mentor.dto.request.MentorPackageRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorPackageResponse;

import java.util.List;
import java.util.UUID;

public interface MentorPackageService {

    MentorPackageResponse createPackage(UUID userId, MentorPackageRequest request);

    MentorPackageResponse updatePackage(UUID packageId, MentorPackageRequest request);

    void deletePackage(UUID packageId);

    MentorPackageResponse toggleActiveStatus(UUID packageId);

    MentorPackageResponse getPackageById(UUID packageId);

    List<MentorPackageResponse> getAllPackagesByMentor(UUID userId);

    List<MentorPackageResponse> getActivePackagesByMentor(UUID userId);
}

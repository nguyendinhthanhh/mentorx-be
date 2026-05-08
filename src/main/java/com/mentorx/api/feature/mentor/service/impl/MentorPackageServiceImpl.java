package com.mentorx.api.feature.mentor.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.mentor.dto.request.MentorPackageRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorPackageResponse;
import com.mentorx.api.feature.mentor.entity.MentorPackage;
import com.mentorx.api.feature.mentor.repository.MentorPackageRepository;
import com.mentorx.api.feature.mentor.service.MentorPackageService;
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
public class MentorPackageServiceImpl implements MentorPackageService {

    private final MentorPackageRepository mentorPackageRepository;
    private final MentorProfileRepository mentorProfileRepository;

    @Override
    @Transactional
    public MentorPackageResponse createPackage(UUID userId, MentorPackageRequest request) {
        log.info("Creating package for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        MentorPackage mentorPackage = new MentorPackage();
        mentorPackage.setMentorProfileId(mentorProfile.getId());
        mentorPackage.setTitle(request.getTitle());
        mentorPackage.setDescription(request.getDescription());
        mentorPackage.setPackageType(request.getPackageType());
        mentorPackage.setDurationHours(request.getDurationHours());
        mentorPackage.setPriceMxc(request.getPriceMxc());
        mentorPackage.setFeatures(request.getFeatures() != null ? 
                request.getFeatures().toArray(new String[0]) : new String[0]);
        mentorPackage.setIsActive(request.getIsActive());
        mentorPackage.setDisplayOrder(request.getDisplayOrder());

        MentorPackage saved = mentorPackageRepository.save(mentorPackage);
        log.info("Package created successfully with ID: {}", saved.getId());

        return MentorPackageResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MentorPackageResponse updatePackage(UUID packageId, MentorPackageRequest request) {
        log.info("Updating package: {}", packageId);

        MentorPackage mentorPackage = mentorPackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        mentorPackage.setTitle(request.getTitle());
        mentorPackage.setDescription(request.getDescription());
        mentorPackage.setPackageType(request.getPackageType());
        mentorPackage.setDurationHours(request.getDurationHours());
        mentorPackage.setPriceMxc(request.getPriceMxc());
        mentorPackage.setFeatures(request.getFeatures() != null ? 
                request.getFeatures().toArray(new String[0]) : new String[0]);
        mentorPackage.setIsActive(request.getIsActive());
        mentorPackage.setDisplayOrder(request.getDisplayOrder());

        MentorPackage updated = mentorPackageRepository.save(mentorPackage);
        log.info("Package updated successfully: {}", packageId);

        return MentorPackageResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deletePackage(UUID packageId) {
        log.info("Deleting package: {}", packageId);

        if (!mentorPackageRepository.existsById(packageId)) {
            throw new AppException(ErrorCode.PACKAGE_NOT_FOUND);
        }

        mentorPackageRepository.deleteById(packageId);
        log.info("Package deleted successfully: {}", packageId);
    }

    @Override
    @Transactional
    public MentorPackageResponse toggleActiveStatus(UUID packageId) {
        log.info("Toggling active status for package: {}", packageId);

        MentorPackage mentorPackage = mentorPackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        mentorPackage.setIsActive(!mentorPackage.getIsActive());
        MentorPackage updated = mentorPackageRepository.save(mentorPackage);

        log.info("Package active status toggled to: {}", updated.getIsActive());
        return MentorPackageResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorPackageResponse getPackageById(UUID packageId) {
        log.info("Getting package by ID: {}", packageId);

        MentorPackage mentorPackage = mentorPackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        return MentorPackageResponse.fromEntity(mentorPackage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorPackageResponse> getAllPackagesByMentor(UUID userId) {
        log.info("Getting all packages for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        List<MentorPackage> packages = mentorPackageRepository
                .findByMentorProfileIdOrderByDisplayOrderAsc(mentorProfile.getId());

        return packages.stream()
                .map(MentorPackageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorPackageResponse> getActivePackagesByMentor(UUID userId) {
        log.info("Getting active packages for user: {}", userId);

        // Get mentor profile by user ID
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        List<MentorPackage> packages = mentorPackageRepository
                .findByMentorProfileIdAndIsActiveTrueOrderByDisplayOrderAsc(mentorProfile.getId());

        return packages.stream()
                .map(MentorPackageResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

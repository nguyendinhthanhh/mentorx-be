package com.mentorx.api.feature.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.exception.KycRejectedException;
import com.mentorx.api.common.exception.ResourceNotFoundException;
import com.mentorx.api.feature.system.service.FileStorageService;
import com.mentorx.api.feature.user.dto.KycStatusResponse;
import com.mentorx.api.feature.user.dto.KycSubmitRequest;
import com.mentorx.api.feature.user.dto.fptai.FptFaceMatchResponse;
import com.mentorx.api.feature.user.dto.fptai.FptLivenessResponse;
import com.mentorx.api.feature.user.dto.fptai.FptOcrResponse;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.model.VerificationMetadataJson;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.FptAiClient;
import com.mentorx.api.feature.user.service.KycService;
import com.mentorx.api.feature.user.service.VideoFrameExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements KycService {

    private final FptAiClient fptAiClient;
    private final VideoFrameExtractorService frameExtractorService;
    private final FileStorageService fileStorageService;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final String KYC_DIR = "kyc";
    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    public KycStatusResponse submitKyc(UUID userId, KycSubmitRequest request) {
        log.info("Submitting KYC for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getMentorStatus() == MentorStatus.KYC_VERIFIED) {
            throw new IllegalStateException("KYC already verified");
        }

        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new MentorProfile for identity verification of user: {}", userId);
                    MentorProfile newProfile = new MentorProfile();
                    newProfile.setUser(user);
                    return mentorProfileRepository.save(newProfile);
                });


        // 1. Store images
        String frontUrl = fileStorageService.store(request.getCccdFront(), KYC_DIR);
        String backUrl = fileStorageService.store(request.getCccdBack(), KYC_DIR);
        profile.setIdentityDocumentUrl(frontUrl);
        profile.setIdentityDocumentBackUrl(backUrl);

        // 2. OCR Front ID Card
        FptOcrResponse ocrResponse = fptAiClient.performOcr(request.getCccdFront());
        if (ocrResponse.errorCode() == 0 && !ocrResponse.data().isEmpty()) {
            FptOcrResponse.FptOcrData ocrData = ocrResponse.data().get(0);
            profile.setLegalName(ocrData.name());
            try {
                profile.setDateOfBirth(LocalDate.parse(ocrData.dob(), DOB_FORMATTER));
            } catch (Exception e) {
                log.warn("Could not parse DOB: {}", ocrData.dob());
            }
            profile.setIdentityDocumentType("CCCD");
        }

        // 3. Extract and check liveness
        MultipartFile frame = frameExtractorService.extractMiddleFrame(request.getLivenessVideo());
        String portraitUrl = fileStorageService.store(frame, KYC_DIR);
        profile.setPortraitUrl(portraitUrl);

        FptLivenessResponse livenessResponse = fptAiClient.checkLiveness(frame);
        if (!livenessResponse.isLive()) {
            user.setMentorStatus(MentorStatus.KYC_REJECTED);
            profile.setRejectionReason("Liveness check failed: " + livenessResponse.message());
            mentorProfileRepository.save(profile);
            userRepository.save(user);
            throw new KycRejectedException("Liveness check failed: " + livenessResponse.message());
        }

        // 4. Face Match
        FptFaceMatchResponse faceMatchResponse = fptAiClient.matchFace(request.getCccdFront(), frame);

        // 5. Build Metadata
        VerificationMetadataJson metadata = new VerificationMetadataJson(
                toJson(ocrResponse),
                null, // No OCR on back image requested by logic flow but could be added
                livenessResponse.livenessScore(),
                livenessResponse.isLive() ? "LIVE" : "FAKE",
                faceMatchResponse.similarity() / 100.0, // FPT AI similarity is 0-100, record expects 0-1.0
                faceMatchResponse.isMatch() ? "MATCHED" : "NOT_MATCHED",
                LocalDateTime.now()
        );

        profile.setVerificationMetadata(toJson(metadata));
        profile.setSubmittedAt(LocalDateTime.now());
        user.setMentorStatus(MentorStatus.KYC_SUBMITTED);

        mentorProfileRepository.save(profile);
        userRepository.save(user);

        return mapToStatusResponse(profile, user);
    }

    @Override
    @Transactional
    public void adminReviewKyc(UUID mentorProfileId, boolean approved, String rejectionReason, UUID adminId) {
        log.info("Admin {} reviewing KYC for profile: {}", adminId, mentorProfileId);

        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found"));
        
        User user = profile.getUser();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        if (approved) {
            user.setMentorStatus(MentorStatus.KYC_VERIFIED);
            user.setIsMentor(true);
            profile.setApprovedAt(LocalDateTime.now());
            profile.setApprovedBy(admin);
        } else {
            user.setMentorStatus(MentorStatus.KYC_REJECTED);
            profile.setRejectionReason(rejectionReason);
        }

        mentorProfileRepository.save(profile);
        userRepository.save(user);
    }

    @Override
    public KycStatusResponse getKycStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElse(null);

        if (profile == null) {
            return new KycStatusResponse(
                    user.getMentorStatus(),
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null, null
            );

        }


        return mapToStatusResponse(profile, user);
    }

    private KycStatusResponse mapToStatusResponse(MentorProfile profile, User user) {
        VerificationMetadataJson metadata = null;
        if (profile.getVerificationMetadata() != null) {
            try {
                metadata = objectMapper.readValue(profile.getVerificationMetadata(), VerificationMetadataJson.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing verification metadata", e);
            }
        }

        return new KycStatusResponse(
                user.getMentorStatus(),
                metadata != null ? metadata.livenessResult() : null,
                metadata != null ? metadata.livenessScore() : null,
                metadata != null ? metadata.faceMatchingResult() : null,
                metadata != null ? metadata.faceMatchingSimilarity() : null,
                profile.getSubmittedAt(),
                profile.getApprovedAt(),
                profile.getRejectionReason(),
                profile.getIdentityDocumentUrl(),
                profile.getIdentityDocumentBackUrl(),
                profile.getPortraitUrl(),
                profile.getLegalName(),
                profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null
        );
    }



    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            return null;
        }
    }
}

package com.mentorx.api.feature.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.IdentityDocumentType;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.exception.KycRejectedException;
import com.mentorx.api.common.exception.ResourceNotFoundException;
import com.mentorx.api.feature.system.service.FileStorageService;
import com.mentorx.api.feature.user.dto.KycStatusResponse;
import com.mentorx.api.feature.user.dto.KycSubmitRequest;
import com.mentorx.api.feature.user.dto.ekyc.EkycFaceMatchResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycLivenessResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycOcrResponse;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.model.VerificationMetadataJson;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.EkycClient;
import com.mentorx.api.feature.user.service.KycService;
import com.mentorx.api.feature.user.service.VideoFrameExtractorService;
import com.mentorx.api.feature.user.util.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements KycService {

    private static final String KYC_DIR = "kyc";
    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EkycClient ekycClient;
    private final VideoFrameExtractorService frameExtractorService;
    private final FileStorageService fileStorageService;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public KycStatusResponse submitKyc(UUID userId, KycSubmitRequest request) {
        log.info("Submitting identity verification for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseGet(() -> mentorProfileRepository.save(MentorProfile.builder().user(user).build()));

        if (profile.getIdentityStatus() == VerificationStatus.APPROVED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Identity verification is already approved.");
        }

        final MultipartFile documentFront;
        final MultipartFile documentBack;
        final MultipartFile livenessVideo;
        try {
            documentFront = toBufferedMultipart(request.getCccdFront(), "documentFront");
            documentBack = toBufferedMultipart(request.getCccdBack(), "documentBack");
            livenessVideo = toBufferedMultipart(request.getLivenessVideo(), "livenessVideo");
        } catch (IOException e) {
            log.error("Failed to read identity verification uploads", e);
            throw new AppException(ErrorCode.BAD_REQUEST, "Unable to read the uploaded verification files.");
        }

        String frontUrl = fileStorageService.store(documentFront, KYC_DIR);
        String backUrl = fileStorageService.store(documentBack, KYC_DIR);
        profile.setIdentityDocumentUrl(frontUrl);
        profile.setIdentityDocumentBackUrl(backUrl);
        profile.setCountryOfResidence(request.getCountry());
        profile.setIdentityDocumentType(resolveDocumentType(request.getDocumentType(), request.getCountry()));
        profile.setDocumentNumberMasked(maskDocumentNumber(request.getDocumentNumber()));

        EkycOcrResponse ocrResponse = ekycClient.performOcr(documentFront);
        if (!ocrResponse.data().isEmpty()) {
            EkycOcrResponse.EkycOcrData ocrData = ocrResponse.data().get(0);
            if (profile.getLegalName() == null || profile.getLegalName().isBlank()) {
                profile.setLegalName(ocrData.name());
            }
            if (profile.getDateOfBirth() == null) {
                try {
                    profile.setDateOfBirth(LocalDate.parse(ocrData.dob(), DOB_FORMATTER));
                } catch (Exception ignored) {
                    log.warn("Could not parse OCR date of birth for user {}", userId);
                }
            }
        }

        MultipartFile frame = frameExtractorService.extractMiddleFrame(livenessVideo);
        String portraitUrl = fileStorageService.store(frame, KYC_DIR);
        profile.setPortraitUrl(portraitUrl);

        EkycFaceMatchResponse faceMatchResponse = ekycClient.matchFaces(documentFront, frame);
        if (!faceMatchResponse.isMatch()) {
            profile.setIdentityStatus(VerificationStatus.REJECTED);
            profile.setIdentityRejectionReason(faceMatchResponse.message());
            mentorProfileRepository.save(profile);
            throw new KycRejectedException(faceMatchResponse.message());
        }

        EkycLivenessResponse livenessResponse = ekycClient.checkLiveness(livenessVideo);
        if (!livenessResponse.isLive()) {
            profile.setIdentityStatus(VerificationStatus.REJECTED);
            profile.setIdentityRejectionReason("Liveness check failed: " + livenessResponse.message());
            mentorProfileRepository.save(profile);
            throw new KycRejectedException("Liveness check failed: " + livenessResponse.message());
        }

        VerificationMetadataJson metadata = new VerificationMetadataJson(
                toJson(ocrResponse),
                null,
                livenessResponse.livenessScore(),
                livenessResponse.isLive() ? "LIVE" : "FAKE",
                faceMatchResponse.similarity() / 100.0,
                faceMatchResponse.isMatch() ? "MATCHED" : "NOT_MATCHED",
                LocalDateTime.now()
        );

        profile.setVerificationMetadata(toJson(metadata));
        profile.setVerificationProvider("internal-ekyc");
        profile.setIdentityStatus(VerificationStatus.PENDING);
        profile.setIdentityRejectionReason(null);
        mentorProfileRepository.save(profile);

        return mapToStatusResponse(profile);
    }

    @Override
    @Transactional
    public void adminReviewKyc(UUID mentorProfileId, boolean approved, String rejectionReason, UUID adminId) {
        log.info("Admin {} reviewing identity verification for mentor profile {}", adminId, mentorProfileId);

        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        if (approved) {
            profile.setIdentityStatus(VerificationStatus.APPROVED);
            profile.setIdentityVerifiedAt(LocalDateTime.now());
            profile.setIdentityVerifiedBy(admin);
            profile.setIdentityRejectionReason(null);
            if (profile.getVerificationProvider() == null || profile.getVerificationProvider().isBlank()) {
                profile.setVerificationProvider("manual-review");
            }
        } else {
            profile.setIdentityStatus(VerificationStatus.REJECTED);
            profile.setIdentityVerifiedAt(null);
            profile.setIdentityVerifiedBy(null);
            profile.setIdentityRejectionReason(rejectionReason);
        }

        mentorProfileRepository.save(profile);
    }

    @Override
    public KycStatusResponse getKycStatus(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MentorProfile profile = mentorProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return new KycStatusResponse(
                    VerificationStatus.NOT_SUBMITTED,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return mapToStatusResponse(profile);
    }

    private KycStatusResponse mapToStatusResponse(MentorProfile profile) {
        VerificationMetadataJson metadata = null;
        if (profile.getVerificationMetadata() != null) {
            try {
                metadata = objectMapper.readValue(profile.getVerificationMetadata(), VerificationMetadataJson.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing verification metadata", e);
            }
        }

        return new KycStatusResponse(
                profile.getIdentityStatus() != null ? profile.getIdentityStatus() : VerificationStatus.NOT_SUBMITTED,
                profile.getIdentityRequired(),
                profile.getIdentityDocumentType(),
                metadata != null ? metadata.livenessResult() : null,
                metadata != null ? metadata.livenessScore() : null,
                metadata != null ? metadata.faceMatchingResult() : null,
                metadata != null ? metadata.faceMatchingSimilarity() : null,
                profile.getSubmittedAt(),
                profile.getIdentityVerifiedAt(),
                profile.getIdentityRejectionReason(),
                profile.getLegalName(),
                profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null,
                profile.getCountryOfResidence(),
                profile.getDocumentNumberMasked(),
                profile.getVerificationProvider()
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

    private static MultipartFile toBufferedMultipart(MultipartFile file, String fieldName) throws IOException {
        byte[] bytes = file.getBytes();
        String orig = file.getOriginalFilename();
        if (orig == null || orig.isBlank()) {
            orig = "upload.bin";
        }
        String ct = file.getContentType();
        return new ByteArrayMultipartFile(
                fieldName,
                orig,
                ct != null ? ct : "application/octet-stream",
                bytes
        );
    }

    private IdentityDocumentType resolveDocumentType(IdentityDocumentType requestedType, String country) {
        if (requestedType != null) {
            return requestedType;
        }
        return country != null && country.trim().equalsIgnoreCase("VN")
                ? IdentityDocumentType.CCCD
                : IdentityDocumentType.PASSPORT;
    }

    private String maskDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            return null;
        }
        String normalized = documentNumber.replaceAll("\\s+", "");
        if (normalized.length() <= 4) {
            return normalized;
        }
        return "*".repeat(Math.max(0, normalized.length() - 4)) + normalized.substring(normalized.length() - 4);
    }
}

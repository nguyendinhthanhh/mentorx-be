package com.mentorx.api.feature.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.IdentityDocumentType;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.service.FileStorageService;
import com.mentorx.api.feature.user.dto.ekyc.EkycFaceMatchResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycOcrResponse;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.EkycClient;
import com.mentorx.api.feature.user.service.EkycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EkycServiceImpl implements EkycService {

    private final EkycClient ekycClient;
    private final FileStorageService fileStorageService;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> verifyIdentity(MultipartFile frontImage, MultipartFile backImage, MultipartFile selfieImage) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            MentorProfile profile = mentorProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> MentorProfile.builder().user(user).build());

            String frontUrl = fileStorageService.storeFile(frontImage);
            String backUrl = fileStorageService.storeFile(backImage);
            String selfieUrl = fileStorageService.storeFile(selfieImage);

            EkycOcrResponse ocrResponse = ekycClient.performOcr(frontImage);
            if (ocrResponse.errorCode() != 0 || ocrResponse.data() == null || ocrResponse.data().isEmpty()) {
                String msg = ocrResponse.errorMessage() != null ? ocrResponse.errorMessage() : "No OCR data";
                return ApiResponse.error("OCR failed: " + msg);
            }

            EkycOcrResponse.EkycOcrData idData = ocrResponse.data().get(0);

            EkycFaceMatchResponse faceMatchResponse = ekycClient.matchFaces(frontImage, selfieImage);
            if (faceMatchResponse.code() != 0) {
                String msg = faceMatchResponse.message() != null ? faceMatchResponse.message() : "Face match error";
                return ApiResponse.error("Face match failed: " + msg);
            }
            if (!faceMatchResponse.isMatch()) {
                return ApiResponse.error(faceMatchResponse.message() != null ? faceMatchResponse.message()
                        : "Ảnh selfie không khớp với ảnh trên CCCD.");
            }

            double similarity = faceMatchResponse.similarity();

            profile.setIdentityDocumentType(IdentityDocumentType.CCCD);
            profile.setIdentityDocumentUrl(frontUrl);
            profile.setIdentityDocumentBackUrl(backUrl);
            profile.setPortraitUrl(selfieUrl);
            profile.setIdentityStatus(VerificationStatus.PENDING);

            String name = idData.name();
            String dobStr = idData.dob();
            if (name != null) {
                profile.setLegalName(name);
            }
            if (dobStr != null) {
                try {
                    profile.setDateOfBirth(LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } catch (Exception e) {
                    log.warn("Could not parse DOB: {}", dobStr);
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ocr", idData);
            metadata.put("faceMatchScore", similarity);
            metadata.put("verifiedAt", java.time.LocalDateTime.now().toString());
            profile.setVerificationMetadata(objectMapper.writeValueAsString(metadata));

            mentorProfileRepository.save(profile);

            Map<String, Object> result = new HashMap<>();
            result.put("similarity", similarity);
            result.put("name", name);
            result.put("dob", dobStr);
            result.put("status", "SUCCESS");

            return ApiResponse.success("Identity verification processed", result);

        } catch (Exception e) {
            log.error("eKYC verification error", e);
            return ApiResponse.error("Verification failed: " + e.getMessage());
        }
    }
}

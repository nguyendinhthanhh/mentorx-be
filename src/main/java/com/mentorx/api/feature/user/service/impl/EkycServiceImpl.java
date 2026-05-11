package com.mentorx.api.feature.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.service.FileStorageService;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.EkycService;
import com.mentorx.api.feature.user.service.FptAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EkycServiceImpl implements EkycService {

    private final FptAiService fptAiService;
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

            // 1. Store files
            String frontUrl = fileStorageService.storeFile(frontImage);
            String backUrl = fileStorageService.storeFile(backImage);
            String selfieUrl = fileStorageService.storeFile(selfieImage);

            // 2. OCR Front ID Card
            Map<String, Object> ocrResult = fptAiService.ocrIdCard(frontImage.getBytes());
            if (ocrResult == null || ocrResult.containsKey("errorCode")) {
                return ApiResponse.error("OCR failed: " + ocrResult.getOrDefault("errorMessage", "Unknown error"));
            }

            // Extract data from FPT AI response (assuming data is in 'data' field)
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) ocrResult.get("data");
            if (dataList == null || dataList.isEmpty()) {
                return ApiResponse.error("No data found on ID card");
            }
            Map<String, Object> idData = dataList.get(0);

            // 3. Face Match
            Map<String, Object> faceMatchResult = fptAiService.faceMatch(frontImage.getBytes(), selfieImage.getBytes());
            if (faceMatchResult == null || faceMatchResult.containsKey("errorCode")) {
                return ApiResponse.error("Face match failed: " + faceMatchResult.getOrDefault("errorMessage", "Unknown error"));
            }

            Map<String, Object> faceMatchData = (Map<String, Object>) faceMatchResult.get("data");
            double similarity = 0;
            if (faceMatchData != null && faceMatchData.get("similarity") != null) {
                similarity = Double.parseDouble(faceMatchData.get("similarity").toString());
            }

            // 4. Update Profile
            profile.setIdentityDocumentType("CCCD");
            profile.setIdentityDocumentUrl(frontUrl);
            profile.setIdentityDocumentBackUrl(backUrl);
            profile.setPortraitUrl(selfieUrl);
            
            // Extract legal name and DOB
            String name = (String) idData.get("name");
            String dobStr = (String) idData.get("dob");
            if (name != null) profile.setLegalName(name);
            if (dobStr != null) {
                try {
                    // FPT AI format is usually DD/MM/YYYY
                    profile.setDateOfBirth(LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } catch (Exception e) {
                    log.warn("Could not parse DOB: {}", dobStr);
                }
            }

            // Save metadata
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
            result.put("status", similarity > 80 ? "SUCCESS" : "MANUAL_REVIEW");

            return ApiResponse.success("Identity verification processed", result);

        } catch (Exception e) {
            log.error("eKYC verification error", e);
            return ApiResponse.error("Verification failed: " + e.getMessage());
        }
    }
}

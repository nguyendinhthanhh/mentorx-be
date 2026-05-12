package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.fptai.FptFaceMatchResponse;
import com.mentorx.api.feature.user.dto.fptai.FptLivenessResponse;
import com.mentorx.api.feature.user.dto.fptai.FptOcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Open-source eKYC implementation using Tesseract OCR and OpenCV
 * 100% FREE - No API key required!
 * 
 * Activate with: -Dspring-boot.run.profiles=dev,opensource-ekyc
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("opensource-ekyc")
public class OpenSourceEkycClient {

    private final OpenSourceOcrService ocrService;
    private final OpenSourceFaceService faceService;

    /**
     * Perform OCR on ID card using Tesseract
     */
    public FptOcrResponse performOcr(MultipartFile image) {
        log.info("🆓 FREE OCR: Using Tesseract (open-source)");
        
        try {
            Map<String, String> extracted = ocrService.extractIdCardInfo(image);
            
            // Convert to FPT AI response format for compatibility
            FptOcrResponse.FptOcrData ocrData = new FptOcrResponse.FptOcrData(
                extracted.getOrDefault("name", "Unknown"),
                extracted.getOrDefault("dob", "01/01/1990"),
                extracted.getOrDefault("idNumber", "000000000000"),
                extracted.getOrDefault("gender", "Unknown"),
                "Vietnam", // nationality
                extracted.getOrDefault("address", "Unknown"),
                extracted.getOrDefault("issueDate", "01/01/2020"),
                extracted.getOrDefault("issuePlace", "Unknown")
            );
            
            log.info("✅ FREE OCR completed successfully");
            return new FptOcrResponse(0, null, List.of(ocrData));
            
        } catch (Exception e) {
            log.error("FREE OCR failed", e);
            return new FptOcrResponse(1, "OCR failed: " + e.getMessage(), List.of());
        }
    }

    /**
     * Check liveness using OpenCV
     */
    public FptLivenessResponse checkLiveness(MultipartFile video) {
        log.info("🆓 FREE Liveness Check: Using OpenCV (open-source)");
        
        try {
            OpenSourceFaceService.LivenessResult result = faceService.checkLiveness(video);
            
            // Convert to FPT AI response format for compatibility
            log.info("✅ FREE Liveness check completed: isLive={}, score={}", 
                    result.isLive(), result.score());
            
            return new FptLivenessResponse(
                result.isLive() ? 0 : 1,
                result.isLive(),
                result.score(),
                result.message()
            );
            
        } catch (Exception e) {
            log.error("FREE Liveness check failed", e);
            return new FptLivenessResponse(1, false, 0.0, "Liveness check failed: " + e.getMessage());
        }
    }

    /**
     * Match faces using OpenCV
     */
    public FptFaceMatchResponse matchFace(MultipartFile image1, MultipartFile image2) {
        log.info("🆓 FREE Face Matching: Using OpenCV (open-source)");
        
        try {
            OpenSourceFaceService.FaceMatchResult result = faceService.matchFaces(image1, image2);
            
            // Convert to FPT AI response format for compatibility
            log.info("✅ FREE Face matching completed: isMatch={}, similarity={}%", 
                    result.isMatch(), result.similarity());
            
            return new FptFaceMatchResponse(
                result.isMatch() ? 0 : 1,
                result.isMatch(),
                result.similarity(),
                result.message()
            );
            
        } catch (Exception e) {
            log.error("FREE Face matching failed", e);
            return new FptFaceMatchResponse(1, false, 0.0, "Face matching failed: " + e.getMessage());
        }
    }

    /**
     * Check if all services are properly configured
     */
    public boolean isConfigured() {
        boolean ocrOk = ocrService.isConfigured();
        boolean faceOk = faceService.isInitialized();
        
        log.info("Open-source eKYC status - OCR: {}, Face: {}", ocrOk, faceOk);
        return ocrOk && faceOk;
    }
}

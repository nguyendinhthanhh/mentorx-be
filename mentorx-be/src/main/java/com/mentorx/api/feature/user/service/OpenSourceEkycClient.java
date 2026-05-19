package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.ekyc.EkycFaceMatchResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycLivenessResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycOcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Local eKYC using Tesseract OCR and OpenCV (no external Vision API).
 * Disabled when profile {@code dev-mock} is active.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!dev-mock")
public class OpenSourceEkycClient implements EkycClient {

    private final OpenSourceOcrService ocrService;
    private final OpenSourceFaceService faceService;

    /**
     * Perform OCR on ID card using Tesseract
     */
    @Override
    public EkycOcrResponse performOcr(MultipartFile image) {
        log.info("Local OCR: Tesseract");
        
        try {
            Map<String, String> extracted = ocrService.extractIdCardInfo(image);
            String addr = extracted.getOrDefault("address", "Unknown");

            EkycOcrResponse.EkycOcrData ocrData = new EkycOcrResponse.EkycOcrData(
                extracted.getOrDefault("idNumber", ""),
                extracted.getOrDefault("name", "Unknown"),
                extracted.getOrDefault("dob", "01/01/1990"),
                extracted.getOrDefault("gender", "Unknown"),
                "Vietnam",
                addr,
                addr,
                ""
            );
            
            log.info("OCR completed");
            return new EkycOcrResponse(0, null, List.of(ocrData));
            
        } catch (Exception e) {
            log.error("OCR failed", e);
            return new EkycOcrResponse(1, "OCR failed: " + e.getMessage(), List.of());
        }
    }

    /**
     * Check liveness using OpenCV
     */
    @Override
    public EkycLivenessResponse checkLiveness(MultipartFile video) {
        log.info("Local liveness: OpenCV");
        
        try {
            OpenSourceFaceService.LivenessResult result = faceService.checkLiveness(video);
            
            log.info("Liveness completed: isLive={}, score={}", result.isLive(), result.score());
            
            return new EkycLivenessResponse(
                result.isLive() ? 0 : 1,
                result.isLive(),
                result.score(),
                result.message()
            );
            
        } catch (Exception e) {
            log.error("Liveness check failed", e);
            return new EkycLivenessResponse(1, false, 0.0, "Liveness check failed: " + e.getMessage());
        }
    }

    /**
     * Match faces using OpenCV
     */
    @Override
    public EkycFaceMatchResponse matchFaces(MultipartFile image1, MultipartFile image2) {
        log.info("Local face match: OpenCV");
        
        try {
            OpenSourceFaceService.FaceMatchResult result = faceService.matchFaces(image1, image2);
            
            log.info("Face match completed: isMatch={}, similarity={}%", result.isMatch(), result.similarity());
            
            return new EkycFaceMatchResponse(
                result.isMatch() ? 0 : 1,
                result.isMatch(),
                result.similarity(),
                result.message()
            );
            
        } catch (Exception e) {
            log.error("Face matching failed", e);
            return new EkycFaceMatchResponse(1, false, 0.0, "Face matching failed: " + e.getMessage());
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

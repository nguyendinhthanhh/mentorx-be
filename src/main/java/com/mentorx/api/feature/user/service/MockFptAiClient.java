package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.fptai.FptFaceMatchResponse;
import com.mentorx.api.feature.user.dto.fptai.FptLivenessResponse;
import com.mentorx.api.feature.user.dto.fptai.FptOcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Mock implementation of FptAiClient for development/testing without valid API key.
 * Activate with: -Dspring-boot.run.profiles=dev,dev-mock
 */
@Service
@Slf4j
@Profile("dev-mock")
public class MockFptAiClient {

    public FptOcrResponse performOcr(MultipartFile image) {
        log.warn("🔶 MOCK MODE: Performing OCR - returning fake CCCD data");
        log.info("Mock OCR - Image: {}, Size: {} bytes", 
                image.getOriginalFilename(), image.getSize());
        
        FptOcrResponse.FptOcrData mockData = new FptOcrResponse.FptOcrData(
            "NGUYEN VAN A",
            "01/01/1990",
            "001234567890",
            "Nam",
            "Ha Noi",
            "123 Nguyen Trai, Thanh Xuan, Ha Noi",
            "01/01/2020",
            "Cuc Canh sat DKQL cu tru va DLQG ve dan cu"
        );
        
        return new FptOcrResponse(0, null, List.of(mockData));
    }

    public FptLivenessResponse checkLiveness(MultipartFile video) {
        log.warn("🔶 MOCK MODE: Checking liveness - returning LIVE with high score");
        log.info("Mock Liveness - Video: {}, Size: {} bytes, Type: {}", 
                video.getOriginalFilename(), video.getSize(), video.getContentType());
        
        return new FptLivenessResponse(
            0,              // code: 0 = success
            true,           // isLive: true
            0.95,           // livenessScore: 95%
            "Mock liveness check passed - person is live"
        );
    }

    public FptFaceMatchResponse matchFace(MultipartFile image1, MultipartFile image2) {
        log.warn("🔶 MOCK MODE: Matching faces - returning MATCH with high similarity");
        log.info("Mock Face Match - Image1: {}, Size: {} bytes", 
                image1.getOriginalFilename(), image1.getSize());
        log.info("Mock Face Match - Image2: {}, Size: {} bytes", 
                image2.getOriginalFilename(), image2.getSize());
        
        return new FptFaceMatchResponse(
            0,              // code: 0 = success
            true,           // isMatch: true
            85.5,           // similarity: 85.5%
            "Mock face match passed - faces are similar"
        );
    }
}

package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.ekyc.EkycFaceMatchResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycLivenessResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycOcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Mock eKYC for development when Tesseract/OpenCV are not available or not desired.
 * Activate with profile {@code dev-mock} (e.g. {@code SPRING_PROFILES_ACTIVE=dev,dev-mock}).
 */
@Service
@Slf4j
@Profile("dev-mock")
public class MockEkycClient implements EkycClient {

    @Override
    public EkycOcrResponse performOcr(MultipartFile image) {
        log.warn("MOCK eKYC: OCR — returning sample CCCD data");
        log.debug("Mock OCR image: {}, {} bytes", image.getOriginalFilename(), image.getSize());

        EkycOcrResponse.EkycOcrData mockData = new EkycOcrResponse.EkycOcrData(
            "001234567890",
            "NGUYEN VAN A",
            "01/01/1990",
            "Nam",
            "Vietnam",
            "123 Nguyen Trai, Thanh Xuan, Ha Noi",
            "123 Nguyen Trai, Thanh Xuan, Ha Noi",
            "01/01/2035"
        );

        return new EkycOcrResponse(0, null, List.of(mockData));
    }

    @Override
    public EkycLivenessResponse checkLiveness(MultipartFile video) {
        log.warn("MOCK eKYC: liveness — returning LIVE");
        log.debug("Mock liveness video: {}, {} bytes", video.getOriginalFilename(), video.getSize());

        return new EkycLivenessResponse(0, true, 0.95, "Mock liveness passed");
    }

    @Override
    public EkycFaceMatchResponse matchFaces(MultipartFile image1, MultipartFile image2) {
        log.warn("MOCK eKYC: face match — returning MATCH");
        log.debug("Mock face match sizes: {} / {} bytes", image1.getSize(), image2.getSize());

        return new EkycFaceMatchResponse(0, true, 85.5, "Mock face match passed");
    }
}

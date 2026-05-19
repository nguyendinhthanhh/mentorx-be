package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.ekyc.EkycFaceMatchResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycLivenessResponse;
import com.mentorx.api.feature.user.dto.ekyc.EkycOcrResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Pluggable eKYC pipeline (OCR, liveness, face match). Default implementation uses local
 * open-source processing (Tesseract + OpenCV). Use profile {@code dev-mock} for tests without native deps.
 */
public interface EkycClient {

    EkycOcrResponse performOcr(MultipartFile image);

    EkycLivenessResponse checkLiveness(MultipartFile video);

    EkycFaceMatchResponse matchFaces(MultipartFile image1, MultipartFile image2);
}

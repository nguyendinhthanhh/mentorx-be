package com.mentorx.api.feature.user.service;

import com.mentorx.api.common.exception.FptAiException;
import com.mentorx.api.feature.user.dto.fptai.FptFaceMatchResponse;
import com.mentorx.api.feature.user.dto.fptai.FptLivenessResponse;
import com.mentorx.api.feature.user.dto.fptai.FptOcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!dev-mock") // Only active when NOT in dev-mock profile
public class FptAiClient {

    private final RestTemplate restTemplate;

    @Value("${fpt.ai.api-key:cfZhosADkpTSwn2veN6XaoZQKAQpPmnx}")
    private String apiKey;

    @Value("${fpt.ai.ocr-url:https://api.fpt.ai/vision/idr/vnm}")
    private String ocrUrl;

    @Value("${fpt.ai.liveness-url:https://api.fpt.ai/vision/ekyc/liveness}")
    private String livenessUrl;

    @Value("${fpt.ai.face-match-url:https://api.fpt.ai/vision/ekyc/facematch/v4}")
    private String faceMatchUrl;

    public FptOcrResponse performOcr(MultipartFile image) {
        log.info("Performing OCR via FPT AI to URL: {}", ocrUrl);
        FptOcrResponse response = postMultipart(ocrUrl, "image", image, FptOcrResponse.class);
        
        // Check for FPT AI error
        if (response.errorCode() != 0) {
            String errorMsg = response.errorMessage() != null ? response.errorMessage() : "Unknown OCR error";
            log.error("FPT AI OCR failed with error code {}: {}", response.errorCode(), errorMsg);
            throw new FptAiException(errorMsg);
        }
        
        return response;
    }

    public FptLivenessResponse checkLiveness(MultipartFile video) {
        log.info("Checking liveness via FPT AI to URL: {}", livenessUrl);
        log.info("Video file - name: {}, size: {} bytes, contentType: {}", 
                video.getOriginalFilename(), video.getSize(), video.getContentType());
        
        // FPT AI liveness v3 expects VIDEO, not image
        FptLivenessResponse response = postMultipart(livenessUrl, "video", video, FptLivenessResponse.class);
        
        log.info("Liveness check response - code: {}, isLive: {}, score: {}", 
                response.code(), response.isLive(), response.livenessScore());
        
        // Check for FPT AI error (code != 0 means error)
        if (response    .code() != 0) {
            String errorMsg = response.message() != null ? response.message() : "Unknown liveness check error";
            log.error("FPT AI Liveness check failed with code {}: {}", response.code(), errorMsg);
            throw new FptAiException(errorMsg);
        }
        
        return response;
    }

    public FptFaceMatchResponse matchFace(MultipartFile image1, MultipartFile image2) {
        log.info("Matching face via FPT AI to URL: {}", faceMatchUrl);
        log.info("Image1 - name: {}, size: {} bytes", image1.getOriginalFilename(), image1.getSize());
        log.info("Image2 - name: {}, size: {} bytes", image2.getOriginalFilename(), image2.getSize());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", apiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            // FPT AI facematch v4 uses 'image1' and 'image2' parameters
            body.add("image1", image1.getResource());
            body.add("image2", image2.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            FptFaceMatchResponse response = restTemplate.postForObject(faceMatchUrl, requestEntity, FptFaceMatchResponse.class);
            
            log.info("Face match response - code: {}, isMatch: {}, similarity: {}", 
                    response != null ? response.code() : "null", 
                    response != null ? response.isMatch() : "null",
                    response != null ? response.similarity() : "null");
            
            // Check for FPT AI error (code != 0 means error)
            if (response != null && response.code() != 0) {
                String errorMsg = response.message() != null ? response.message() : "Unknown face matching error";
                log.error("FPT AI Face Match failed with code {}: {}", response.code(), errorMsg);
                throw new FptAiException(errorMsg);
            }
            
            return response;
        } catch (RestClientException e) {
            log.error("FPT AI Face Match request failed", e);
            throw new FptAiException("FPT AI Face Match request failed: " + e.getMessage());
        }
    }

    private <T> T postMultipart(String url, String fileParamName, MultipartFile file, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", apiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(fileParamName, file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, requestEntity, responseType);
        } catch (RestClientException e) {
            log.error("FPT AI request failed to URL: {}", url, e);
            throw new FptAiException("FPT AI request failed: " + e.getMessage());
        }
    }
}

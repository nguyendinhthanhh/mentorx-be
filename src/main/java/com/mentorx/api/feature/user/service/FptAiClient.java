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
public class FptAiClient {

    private final RestTemplate restTemplate;

    @Value("${fpt.ai.api-key}")
    private String apiKey;

    @Value("${fpt.ai.ocr-url}")
    private String ocrUrl;

    @Value("${fpt.ai.liveness-url}")
    private String livenessUrl;

    @Value("${fpt.ai.face-match-url}")
    private String faceMatchUrl;

    public FptOcrResponse performOcr(MultipartFile image) {
        log.info("Performing OCR via FPT AI");
        return postMultipart(ocrUrl, "image", image, FptOcrResponse.class);
    }

    public FptLivenessResponse checkLiveness(MultipartFile videoFrame) {
        log.info("Checking liveness via FPT AI");
        return postMultipart(livenessUrl, "image", videoFrame, FptLivenessResponse.class);
    }

    public FptFaceMatchResponse matchFace(MultipartFile image1, MultipartFile image2) {
        log.info("Matching face via FPT AI");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", apiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("front", image1.getResource());
            body.add("back", image2.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(faceMatchUrl, requestEntity, FptFaceMatchResponse.class);
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

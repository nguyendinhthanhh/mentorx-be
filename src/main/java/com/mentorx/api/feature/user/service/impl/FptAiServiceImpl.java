package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.config.FptAiConfig;
import com.mentorx.api.feature.user.service.FptAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FptAiServiceImpl implements FptAiService {

    private final FptAiConfig fptAiConfig;
    private final RestTemplate restTemplate;

    @Override
    public Map<String, Object> ocrIdCard(byte[] imageBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", fptAiConfig.getApiKey());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "id_card.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Calling FPT AI OCR API...");
            return restTemplate.postForObject(fptAiConfig.getOcrUrl(), requestEntity, Map.class);
        } catch (Exception e) {
            log.error("Error calling FPT AI OCR API", e);
            return Map.of("errorCode", 500, "errorMessage", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> faceMatch(byte[] image1Bytes, byte[] image2Bytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", fptAiConfig.getApiKey());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image[]", new ByteArrayResource(image1Bytes) {
                @Override
                public String getFilename() {
                    return "image1.jpg";
                }
            });
            body.add("image[]", new ByteArrayResource(image2Bytes) {
                @Override
                public String getFilename() {
                    return "image2.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Calling FPT AI Face Match API...");
            return restTemplate.postForObject(fptAiConfig.getFaceMatchUrl(), requestEntity, Map.class);
        } catch (Exception e) {
            log.error("Error calling FPT AI Face Match API", e);
            return Map.of("errorCode", 500, "errorMessage", e.getMessage());
        }
    }
}

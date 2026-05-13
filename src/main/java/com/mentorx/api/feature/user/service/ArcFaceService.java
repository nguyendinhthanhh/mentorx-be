package com.mentorx.api.feature.user.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Service
@Slf4j
public class ArcFaceService {

    private final RestTemplate restTemplate;

    @Value("${app.kyc.face-service-url:http://127.0.0.1:8091}")
    private String faceServiceUrl;

    public ArcFaceService(
            @Value("${app.kyc.face-service-connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${app.kyc.face-service-read-timeout-ms:45000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restTemplate = new RestTemplate(factory);
    }

    public FaceVerifyResult verify(MultipartFile image1, MultipartFile image2) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image1", filePart(image1));
            body.add("image2", filePart(image2));

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ArcFaceResponse response = restTemplate.postForObject(
                    normalizeBaseUrl(faceServiceUrl) + "/face/verify",
                    request,
                    ArcFaceResponse.class
            );

            if (response == null) {
                return FaceVerifyResult.needsReview(0.0, "Không nhận được phản hồi từ ArcFace service.");
            }

            log.info("ArcFace response: match={} review={} similarity={} message={}",
                    response.isMatch(), response.needsReview(), response.similarity(), response.message());

            if (response.needsReview()) {
                return FaceVerifyResult.needsReview(response.similarity(), response.message());
            }
            if (response.isMatch()) {
                return FaceVerifyResult.match(response.similarity(), response.message());
            }
            return FaceVerifyResult.noMatch(response.similarity(), response.message());
        } catch (IOException e) {
            log.warn("ArcFace multipart build failed: {}", e.getMessage());
            return FaceVerifyResult.needsReview(0.0, "Không đọc được ảnh để gửi sang ArcFace service.");
        } catch (RestClientException e) {
            log.warn("ArcFace service unavailable: {}", e.getMessage());
            return FaceVerifyResult.needsReview(0.0,
                    "Dịch vụ so khớp khuôn mặt ArcFace chưa sẵn sàng. Hồ sơ cần admin kiểm tra thủ công.");
        }
    }

    private static HttpEntity<ByteArrayResource> filePart(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        headers.setContentType(contentType);
        return new HttpEntity<>(new NamedByteArrayResource(file.getBytes(), safeName(file)), headers);
    }

    private static String safeName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name == null || name.isBlank() ? "image.jpg" : name;
    }

    private static String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

    public record FaceVerifyResult(
            boolean isMatch,
            boolean needsReview,
            double similarity,
            String message
    ) {
        static FaceVerifyResult match(double similarity, String message) {
            return new FaceVerifyResult(true, false, similarity, message);
        }

        static FaceVerifyResult noMatch(double similarity, String message) {
            return new FaceVerifyResult(false, false, similarity, message);
        }

        static FaceVerifyResult needsReview(double similarity, String message) {
            return new FaceVerifyResult(false, true, similarity, message);
        }
    }

    private record ArcFaceResponse(
            @JsonProperty("is_match") boolean isMatch,
            @JsonProperty("needs_review") boolean needsReview,
            double similarity,
            double threshold,
            @JsonProperty("review_threshold") double reviewThreshold,
            @JsonProperty("face_count_1") int faceCount1,
            @JsonProperty("face_count_2") int faceCount2,
            String message
    ) {
    }
}

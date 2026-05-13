package com.mentorx.api.feature.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Partner REST settings for VNPT eKYC. URLs and secrets come from env; see {@code application-vnpt-ekyc.yml}.
 */
@ConfigurationProperties(prefix = "app.vnpt-ekyc")
public record VnptEkycProperties(
        String baseUrl,
        String ocrUrl,
        String faceMatchUrl,
        String livenessUrl,
        String accessToken,
        String tokenId,
        String tokenKey,
        String publicKeyCa,
        String authorizationHeader,
        String tokenIdHeader,
        String tokenKeyHeader,
        String ocrImagePart,
        String faceImage1Part,
        String faceImage2Part,
        String livenessVideoPart,
        int connectTimeoutMs,
        int readTimeoutMs,
        double faceMatchApproveSimilarityMin,
        double faceMatchReviewSimilarityMin
) {

    public VnptEkycProperties {
        if (!StringUtils.hasText(authorizationHeader)) {
            authorizationHeader = "Authorization";
        }
        if (!StringUtils.hasText(tokenIdHeader)) {
            tokenIdHeader = "token-id";
        }
        if (!StringUtils.hasText(tokenKeyHeader)) {
            tokenKeyHeader = "token-key";
        }
        if (!StringUtils.hasText(ocrImagePart)) {
            ocrImagePart = "image";
        }
        if (!StringUtils.hasText(faceImage1Part)) {
            faceImage1Part = "image1";
        }
        if (!StringUtils.hasText(faceImage2Part)) {
            faceImage2Part = "image2";
        }
        if (!StringUtils.hasText(livenessVideoPart)) {
            livenessVideoPart = "video";
        }
        if (connectTimeoutMs <= 0) {
            connectTimeoutMs = 15_000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 120_000;
        }
        if (faceMatchApproveSimilarityMin <= 0) {
            faceMatchApproveSimilarityMin = 56.0;
        }
        if (faceMatchReviewSimilarityMin <= 0) {
            faceMatchReviewSimilarityMin = 42.0;
        }
    }

    public void requireConfigured() {
        if (!StringUtils.hasText(ocrUrl)) {
            throw new IllegalStateException("app.vnpt-ekyc.ocr-url (or VNPT_EKYC_OCR_URL) is required when profile vnpt-ekyc is active");
        }
        if (!StringUtils.hasText(faceMatchUrl)) {
            throw new IllegalStateException("app.vnpt-ekyc.face-match-url (or VNPT_EKYC_FACE_MATCH_URL) is required when profile vnpt-ekyc is active");
        }
        if (!StringUtils.hasText(livenessUrl)) {
            throw new IllegalStateException("app.vnpt-ekyc.liveness-url (or VNPT_EKYC_LIVENESS_URL) is required when profile vnpt-ekyc is active");
        }
        if (!StringUtils.hasText(accessToken)) {
            throw new IllegalStateException("app.vnpt-ekyc.access-token (or VNPT_EKYC_ACCESS_TOKEN) is required when profile vnpt-ekyc is active");
        }
        if (!StringUtils.hasText(tokenId)) {
            throw new IllegalStateException("app.vnpt-ekyc.token-id (or VNPT_EKYC_TOKEN_ID) is required when profile vnpt-ekyc is active");
        }
    }

    public String resolveUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String t = url.trim();
        if (t.startsWith("http://") || t.startsWith("https://")) {
            return t;
        }
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalStateException("app.vnpt-ekyc.base-url is required when OCR/face/liveness URLs are relative paths");
        }
        String base = baseUrl.trim().replaceAll("/+$", "");
        String path = t.replaceAll("^/+", "");
        return base + "/" + path;
    }
}

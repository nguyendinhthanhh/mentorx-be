package com.mentorx.api.feature.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.config.CloudinaryProperties;
import com.mentorx.api.feature.system.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final CloudinaryProperties cloudinaryProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String storeFile(MultipartFile file) {
        return store(file, null);
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        validateConfiguration();
        validateFile(file);

        String folder = normalizeFolder(subDirectory);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        Map<String, String> paramsToSign = new TreeMap<>();
        paramsToSign.put("timestamp", timestamp);
        if (folder != null) {
            paramsToSign.put("folder", folder);
        }

        String signature = createSignature(paramsToSign);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ByteArrayMultipartFileResource(file));
        bodyBuilder.part("api_key", cloudinaryProperties.getApiKey());
        bodyBuilder.part("timestamp", timestamp);
        bodyBuilder.part("signature", signature);
        if (folder != null) {
            bodyBuilder.part("folder", folder);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        String uploadUrl = buildUploadUrl();
        HttpEntity<?> requestEntity = new HttpEntity<>(bodyBuilder.build(), headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, JsonNode.class);
            JsonNode body = response.getBody();
            if (body == null) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Cloudinary upload returned an empty response.");
            }

            if (body.has("error")) {
                String message = body.path("error").path("message").asText("Cloudinary upload failed.");
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, message);
            }

            String secureUrl = body.path("secure_url").asText(null);
            if (!StringUtils.hasText(secureUrl)) {
                secureUrl = body.path("url").asText(null);
            }

            if (!StringUtils.hasText(secureUrl)) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Cloudinary upload did not return a file URL.");
            }

            return secureUrl;
        } catch (RestClientException ex) {
            log.error("Cloudinary upload failed", ex);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Could not upload file to Cloudinary. Please try again!");
        }
    }

    @Override
    public void deleteFile(String fileName) {
        log.warn("Cloudinary file deletion is not wired for this reference type: {}", fileName);
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(cloudinaryProperties.getCloudName())
                || !StringUtils.hasText(cloudinaryProperties.getApiKey())
                || !StringUtils.hasText(cloudinaryProperties.getApiSecret())) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Cloudinary configuration is missing. Set cloud name, API key, and API secret.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "File is empty.");
        }
    }

    private String normalizeFolder(String subDirectory) {
        if (!StringUtils.hasText(subDirectory)) {
            return null;
        }

        String normalized = StringUtils.cleanPath(subDirectory.trim()).replace('\\', '/');
        normalized = normalized.replaceAll("^/+", "").replaceAll("/+$", "");
        if (normalized.isBlank() || normalized.contains("..")) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid folder name.");
        }

        return normalized;
    }

    private String buildUploadUrl() {
        String baseUrl = cloudinaryProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "https://api.cloudinary.com";
        }
        return baseUrl.replaceAll("/+$", "") + "/v1_1/" + cloudinaryProperties.getCloudName() + "/auto/upload";
    }

    private String createSignature(Map<String, String> params) {
        StringBuilder payload = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!StringUtils.hasText(entry.getValue())) {
                continue;
            }
            if (!first) {
                payload.append('&');
            }
            payload.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        payload.append(cloudinaryProperties.getApiSecret());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unable to sign Cloudinary upload request.");
        }
    }

    private static final class ByteArrayMultipartFileResource extends org.springframework.core.io.ByteArrayResource {
        private final String filename;

        private ByteArrayMultipartFileResource(MultipartFile file) {
            super(readBytes(file));
            this.filename = StringUtils.cleanPath(
                    StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "upload"
            );
        }

        @Override
        public String getFilename() {
            return filename;
        }

        private static byte[] readBytes(MultipartFile file) {
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Could not read uploaded file.");
            }
        }
    }
}

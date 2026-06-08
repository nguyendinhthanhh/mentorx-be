package com.mentorx.api.common.util;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.dto.response.FileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryMediaServiceImpl implements CloudinaryMediaService {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    private final RestTemplate restTemplate;

    @Override
    public FileResponse uploadCourseMedia(MultipartFile file, String folder) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cloudinary is not configured");
        }
        String safeFolder = (folder == null || folder.isBlank()) ? "mentorx/courses" : folder;
        long timestamp = System.currentTimeMillis() / 1000;
        String signatureBase = "folder=" + safeFolder + "&timestamp=" + timestamp + apiSecret;
        String signature = sha1(signatureBase);
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload";

        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();
            body.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.part("api_key", apiKey);
            body.part("timestamp", String.valueOf(timestamp));
            body.part("folder", safeFolder);
            body.part("signature", signature);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, body.build(), Map.class);
            if (response == null || response.get("secure_url") == null) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Cloudinary did not return a secure URL");
            }
            return FileResponse.builder()
                    .fileName(String.valueOf(response.getOrDefault("public_id", file.getOriginalFilename())))
                    .fileUrl(String.valueOf(response.get("secure_url")))
                    .fileType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType())
                    .size(file.getSize())
                    .build();
        } catch (IOException ex) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Could not read uploaded file", ex);
        }
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Could not sign Cloudinary upload", ex);
        }
    }
}

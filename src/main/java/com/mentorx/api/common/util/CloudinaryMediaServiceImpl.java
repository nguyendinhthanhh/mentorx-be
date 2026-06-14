package com.mentorx.api.common.util;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.dto.response.CloudinarySignedUploadResponse;
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
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    public CloudinarySignedUploadResponse createSignedUpload(String folder) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cloudinary is not configured");
        }
        String safeFolder = (folder == null || folder.isBlank()) ? "mentorx/courses" : folder;
        long timestamp = System.currentTimeMillis() / 1000;
        String signatureBase = "folder=" + safeFolder + "&timestamp=" + timestamp + apiSecret;
        String signature = sha1(signatureBase);
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload";

        return CloudinarySignedUploadResponse.builder()
                .cloudName(cloudName)
                .apiKey(apiKey)
                .timestamp(timestamp)
                .folder(safeFolder)
                .signature(signature)
                .uploadUrl(url)
                .build();
    }

    @Override
    public FileResponse uploadCourseMedia(MultipartFile file, String folder) {
        CloudinarySignedUploadResponse signedUpload = createSignedUpload(folder);

        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();
            body.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.part("api_key", signedUpload.getApiKey());
            body.part("timestamp", String.valueOf(signedUpload.getTimestamp()));
            body.part("folder", signedUpload.getFolder());
            body.part("signature", signedUpload.getSignature());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(signedUpload.getUploadUrl(), body.build(), Map.class);
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

    @Override
    public void deleteCourseMedia(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cloudinary is not configured");
        }

        CloudinaryAsset asset = parseAsset(fileUrl);
        if (asset == null) {
            return;
        }

        long timestamp = System.currentTimeMillis() / 1000;
        String signatureBase = "public_id=" + asset.publicId() + "&timestamp=" + timestamp + apiSecret;
        String signature = sha1(signatureBase);
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/" + asset.resourceType() + "/destroy";

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("public_id", asset.publicId());
        body.part("api_key", apiKey);
        body.part("timestamp", String.valueOf(timestamp));
        body.part("signature", signature);
        restTemplate.postForObject(url, body.build(), Map.class);
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

    private CloudinaryAsset parseAsset(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String[] parts = uri.getPath().split("/");
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }
            if (uploadIndex < 2 || uploadIndex + 1 >= parts.length) {
                return null;
            }
            String resourceType = parts[uploadIndex - 1];
            int publicIdStart = uploadIndex + 1;
            if (parts[publicIdStart].matches("v\\d+")) {
                publicIdStart++;
            }
            if (publicIdStart >= parts.length) {
                return null;
            }
            StringBuilder publicId = new StringBuilder();
            for (int i = publicIdStart; i < parts.length; i++) {
                if (publicId.length() > 0) {
                    publicId.append('/');
                }
                publicId.append(parts[i]);
            }
            String decodedPublicId = URLDecoder.decode(publicId.toString(), StandardCharsets.UTF_8);
            int extensionIndex = decodedPublicId.lastIndexOf('.');
            if (extensionIndex > 0) {
                decodedPublicId = decodedPublicId.substring(0, extensionIndex);
            }
            return new CloudinaryAsset(resourceType, decodedPublicId);
        } catch (Exception ex) {
            return null;
        }
    }

    private record CloudinaryAsset(String resourceType, String publicId) {
    }
}

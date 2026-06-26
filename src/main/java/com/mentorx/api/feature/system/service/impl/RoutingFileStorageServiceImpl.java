package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.feature.system.config.CloudinaryProperties;
import com.mentorx.api.feature.system.config.FileStorageProperties;
import com.mentorx.api.feature.system.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class RoutingFileStorageServiceImpl implements FileStorageService {

    private static final Set<String> PUBLIC_CLOUD_FOLDERS = Set.of(
            "public",
            "public/avatar",
            "public/mentor-profile",
            "public/course-thumbnail",
            "public/mentor-asset",
            "public/portfolio",
            "public/cover",
            "public/chat"
    );

    private static final Set<String> LOCAL_ONLY_FOLDERS = Set.of(
            "kyc",
            "private",
            "private/document",
            "private/documents",
            "private/video",
            "private/cv",
            "private/certificate"
    );

    private final LocalFileStorageServiceImpl localFileStorageService;
    private final CloudinaryFileStorageServiceImpl cloudinaryFileStorageService;
    private final FileStorageProperties fileStorageProperties;
    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public String storeFile(MultipartFile file) {
        return store(file, null);
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        if (shouldUseCloudinary(file, subDirectory)) {
            try {
                return cloudinaryFileStorageService.store(file, subDirectory);
            } catch (RuntimeException ex) {
                log.warn("Cloudinary upload failed, falling back to local storage: {}", ex.getMessage());
            }
        }

        return localFileStorageService.store(file, subDirectory);
    }

    @Override
    public void deleteFile(String fileName) {
        if (shouldUseCloudinary()) {
            cloudinaryFileStorageService.deleteFile(fileName);
            return;
        }

        localFileStorageService.deleteFile(fileName);
    }

    private boolean shouldUseCloudinary() {
        String provider = fileStorageProperties.getStorageProvider();
        boolean explicitlyLocal = StringUtils.hasText(provider) && "local".equalsIgnoreCase(provider.trim());
        if (explicitlyLocal) {
            return false;
        }

        return StringUtils.hasText(cloudinaryProperties.getCloudName())
                && StringUtils.hasText(cloudinaryProperties.getApiKey())
                && StringUtils.hasText(cloudinaryProperties.getApiSecret());
    }

    private boolean shouldUseCloudinary(MultipartFile file, String subDirectory) {
        return shouldUseCloudinary();
    }
}

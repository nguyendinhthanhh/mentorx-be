package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.config.FileStorageProperties;
import com.mentorx.api.feature.system.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Could not create the directory where the uploaded files will be stored.");
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        return store(file, null);
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        // Normalize file name (browser blobs / camera may omit originalFilename)
        String rawName = file.getOriginalFilename();
        String safeName = (rawName == null || rawName.isBlank()) ? "upload" : rawName;
        String originalFileName = StringUtils.cleanPath(safeName);
        
        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Filename contains invalid path sequence " + originalFileName);
            }

            // Generate a unique file name to avoid collisions
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // Handle subdirectory
            Path targetDir = this.fileStorageLocation;
            if (subDirectory != null && !subDirectory.isEmpty()) {
                targetDir = targetDir.resolve(subDirectory);
                Files.createDirectories(targetDir);
            }

            // Copy file to the target location
            Path targetLocation = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path including subdirectory
            return (subDirectory != null && !subDirectory.isEmpty()) 
                ? subDirectory + "/" + fileName 
                : fileName;
                
        } catch (IOException ex) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Could not store file " + originalFileName + ". Please try again!");
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileName, ex);
        }
    }

}

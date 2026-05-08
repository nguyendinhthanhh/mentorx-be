package com.mentorx.api.feature.system.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    void deleteFile(String fileName);
}

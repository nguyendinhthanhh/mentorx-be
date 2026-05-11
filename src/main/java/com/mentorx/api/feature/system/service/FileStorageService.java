package com.mentorx.api.feature.system.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    String store(MultipartFile file, String subDirectory);
    void deleteFile(String fileName);

}

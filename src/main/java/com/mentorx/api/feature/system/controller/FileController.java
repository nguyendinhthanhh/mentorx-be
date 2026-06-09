package com.mentorx.api.feature.system.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.dto.response.CloudinarySignedUploadResponse;
import com.mentorx.api.feature.system.dto.response.FileResponse;
import com.mentorx.api.common.util.CloudinaryMediaService;
import com.mentorx.api.feature.system.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final CloudinaryMediaService cloudinaryMediaService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        FileResponse response = FileResponse.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileType(file.getContentType())
                .size(file.getSize())
                .build();

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    @PostMapping("/course-media")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<FileResponse>> uploadCourseMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        return ResponseEntity.ok(ApiResponse.success(
                "Course media uploaded successfully",
                cloudinaryMediaService.uploadCourseMedia(file, folder)
        ));
    }

    @PostMapping("/course-media/sign")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CloudinarySignedUploadResponse>> signCourseMediaUpload(
            @RequestParam(value = "folder", required = false) String folder) {
        return ResponseEntity.ok(ApiResponse.success(
                "Course media upload signed successfully",
                cloudinaryMediaService.createSignedUpload(folder)
        ));
    }

    @PostMapping("/course-media/delete")
    @PreAuthorize("hasAnyRole('MENTOR', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourseMedia(@RequestParam("fileUrl") String fileUrl) {
        cloudinaryMediaService.deleteCourseMedia(fileUrl);
        return ResponseEntity.ok(ApiResponse.success("Course media deleted successfully", null));
    }
}

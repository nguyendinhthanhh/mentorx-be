package com.mentorx.api.common.util;

import com.mentorx.api.feature.system.dto.response.FileResponse;
import com.mentorx.api.feature.system.dto.response.CloudinarySignedUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryMediaService {
    CloudinarySignedUploadResponse createSignedUpload(String folder);
    FileResponse uploadCourseMedia(MultipartFile file, String folder);
    void deleteCourseMedia(String fileUrl);
}

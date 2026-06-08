package com.mentorx.api.common.util;

import com.mentorx.api.feature.system.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryMediaService {
    FileResponse uploadCourseMedia(MultipartFile file, String folder);
}

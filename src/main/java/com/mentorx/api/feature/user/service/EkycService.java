package com.mentorx.api.feature.user.service;

import com.mentorx.api.common.response.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EkycService {
    ApiResponse<Map<String, Object>> verifyIdentity(MultipartFile frontImage, MultipartFile backImage, MultipartFile selfieImage);
}

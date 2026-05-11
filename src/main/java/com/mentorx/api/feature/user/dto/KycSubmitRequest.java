package com.mentorx.api.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmitRequest {
    private MultipartFile cccdFront;
    private MultipartFile cccdBack;
    private MultipartFile livenessVideo;
}

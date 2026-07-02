package com.mentorx.api.feature.system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudinarySignedUploadResponse {
    private String cloudName;
    private String apiKey;
    private long timestamp;
    private String folder;
    private String signature;
    private String uploadUrl;
}

package com.mentorx.api.feature.user.dto.ekyc;

import java.util.List;

public record EkycOcrResponse(
    int errorCode,
    String errorMessage,
    List<EkycOcrData> data
) {
    public record EkycOcrData(
        String id,
        String name,
        String dob,
        String sex,
        String nationality,
        String home,
        String address,
        String doe
    ) {}
}

package com.mentorx.api.feature.user.dto.fptai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FptOcrResponse(
    int errorCode,
    String errorMessage,
    List<FptOcrData> data
) {
    public record FptOcrData(
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

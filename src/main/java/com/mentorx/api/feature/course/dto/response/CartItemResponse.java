package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private UUID id;
    private UUID userId;
    private UUID courseId;
    private String courseTitle;
    private String courseThumbnail;
    private BigDecimal coursePriceMxc;
    private String instructorName;
    private LocalDateTime addedAt;
}

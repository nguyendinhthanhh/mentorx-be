package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSectionResponse {

    private UUID id;
    private UUID courseId;
    private String title;
    private String description;
    private Integer sectionOrder;
    private Integer durationMinutes;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

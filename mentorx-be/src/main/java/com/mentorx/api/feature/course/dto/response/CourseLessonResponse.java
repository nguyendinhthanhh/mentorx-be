package com.mentorx.api.feature.course.dto.response;

import com.mentorx.api.common.enums.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLessonResponse {

    private UUID id;
    private UUID sectionId;
    private String title;
    private String description;
    private LessonType lessonType;
    private Integer lessonOrder;
    private Integer durationMinutes;
    private String videoUrl;
    private String articleContent;
    private String resourceUrl;
    private Boolean isFreePreview;
    private Boolean isPublished;
    private Boolean isMandatory;
    private Map<String, Object> metadata;
    private Integer viewCount;
    private Integer avgCompletionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

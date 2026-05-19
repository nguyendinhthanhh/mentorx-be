package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.LessonType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLessonCreateRequest {

    @NotNull(message = "Section ID is required")
    private UUID sectionId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Lesson type is required")
    private LessonType lessonType;

    @NotNull(message = "Lesson order is required")
    @Min(value = 1, message = "Lesson order must be at least 1")
    private Integer lessonOrder;

    @Min(value = 0, message = "Duration must be at least 0")
    private Integer durationMinutes;

    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoUrl;

    private String articleContent;

    @Size(max = 500, message = "Resource URL must not exceed 500 characters")
    private String resourceUrl;

    private Boolean isFreePreview;

    private Boolean isPublished;

    private Boolean isMandatory;

    private Map<String, Object> metadata;
}

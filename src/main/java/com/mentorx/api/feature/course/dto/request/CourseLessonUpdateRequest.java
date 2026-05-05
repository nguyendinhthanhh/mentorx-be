package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLessonUpdateRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private LessonType lessonType;

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

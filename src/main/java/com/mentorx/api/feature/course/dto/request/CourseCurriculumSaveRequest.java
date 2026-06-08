package com.mentorx.api.feature.course.dto.request;

import com.mentorx.api.common.enums.LessonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCurriculumSaveRequest {

    @NotEmpty(message = "At least one section is required")
    @Valid
    private List<SectionItem> sections;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectionItem {
        private UUID id;

        @NotBlank(message = "Section title is required")
        @Size(max = 200, message = "Section title must not exceed 200 characters")
        private String title;

        @Size(max = 1000, message = "Section description must not exceed 1000 characters")
        private String description;

        @NotNull
        @Min(1)
        private Integer sectionOrder;

        private Boolean isPublished;

        @Valid
        private List<LessonItem> lessons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonItem {
        private UUID id;

        @NotBlank(message = "Lesson title is required")
        @Size(max = 200, message = "Lesson title must not exceed 200 characters")
        private String title;

        @Size(max = 2000, message = "Lesson description must not exceed 2000 characters")
        private String description;

        @NotNull(message = "Lesson type is required")
        private LessonType lessonType;

        @NotNull
        @Min(1)
        private Integer lessonOrder;

        @Min(0)
        private Integer durationMinutes;

        private String videoUrl;
        private String articleContent;
        private String resourceUrl;
        private Boolean isFreePreview;
        private Boolean isPublished;
        private Boolean isMandatory;
        private Map<String, Object> metadata;
    }
}

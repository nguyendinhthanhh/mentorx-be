package com.mentorx.api.feature.course.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCommentUpdateRequest {

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;
}

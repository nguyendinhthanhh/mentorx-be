package com.mentorx.api.feature.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateWithCurriculumRequest {

    @NotNull
    @Valid
    private CourseCreateRequest course;

    @Valid
    private CourseCurriculumSaveRequest curriculum;
}

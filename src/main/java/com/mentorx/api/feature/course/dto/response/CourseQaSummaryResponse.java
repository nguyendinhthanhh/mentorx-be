package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseQaSummaryResponse {
    private UUID courseId;
    private int unansweredLearners;
}

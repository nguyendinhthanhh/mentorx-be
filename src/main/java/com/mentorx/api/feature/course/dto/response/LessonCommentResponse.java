package com.mentorx.api.feature.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCommentResponse {

    private UUID id;
    private UUID lessonId;
    private UUID userId;
    private String userName;
    private String userAvatar;
    private UUID parentId;
    private String content;
    private Boolean isDeleted;
    private List<LessonCommentResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

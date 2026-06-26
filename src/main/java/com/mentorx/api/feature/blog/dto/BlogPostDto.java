package com.mentorx.api.feature.blog.dto;

import com.mentorx.api.feature.blog.enums.BlogAudience;
import com.mentorx.api.feature.blog.enums.BlogCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BlogPostDto {
    private UUID id;
    private String slug;
    private String title;
    private String excerpt;
    private BlogCategory category;
    private BlogAudience audience;
    private String author;
    private String authorRole;
    private String authorAvatar;
    private String coverImage;
    private String content;
    private String readTime;
    private Boolean featured;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

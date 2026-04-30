package com.mentorx.api.feature.course.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.LessonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a lesson within a course section
 * Supports multiple lesson types: video, article, quiz, assignment, live session
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "course_lessons", indexes = {
    @Index(name = "idx_lesson_section_id", columnList = "section_id"),
    @Index(name = "idx_lesson_type", columnList = "lesson_type"),
    @Index(name = "idx_lesson_order", columnList = "lesson_order"),
    @Index(name = "idx_lesson_published", columnList = "is_published"),
    @Index(name = "idx_lesson_free_preview", columnList = "is_free_preview")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLesson extends BaseEntity {

    /**
     * The section this lesson belongs to
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    /**
     * Lesson title
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Lesson description/summary
     */
    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Type of lesson (video, article, quiz, assignment, live_session)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 20)
    private LessonType lessonType;

    /**
     * Order/sequence within the section
     */
    @NotNull
    @Min(value = 1)
    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder;

    /**
     * Duration in minutes (for video/live sessions)
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Video URL (for video lessons)
     */
    @Size(max = 500)
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /**
     * Article content (for article lessons)
     */
    @Column(name = "article_content", columnDefinition = "TEXT")
    private String articleContent;

    /**
     * External resource URL
     */
    @Size(max = 500)
    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    /**
     * Whether this lesson is available as free preview
     */
    @Column(name = "is_free_preview", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFreePreview = false;

    /**
     * Whether this lesson is published
     */
    @Column(name = "is_published", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isPublished = true;

    /**
     * Whether this lesson is mandatory for course completion
     */
    @Column(name = "is_mandatory", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isMandatory = true;

    /**
     * Additional metadata (quiz questions, assignment details, etc.)
     * Stored as JSONB for flexibility
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Number of views/completions
     */
    @Column(name = "view_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer viewCount = 0;

    /**
     * Average completion time in minutes
     */
    @Column(name = "avg_completion_time")
    private Integer avgCompletionTime;

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Checks if this lesson is a video lesson
     */
    public boolean isVideoLesson() {
        return LessonType.VIDEO.equals(this.lessonType);
    }

    /**
     * Checks if this lesson is an article
     */
    public boolean isArticle() {
        return LessonType.ARTICLE.equals(this.lessonType);
    }

    /**
     * Checks if this lesson is a quiz
     */
    public boolean isQuiz() {
        return LessonType.QUIZ.equals(this.lessonType);
    }

    /**
     * Checks if this lesson is an assignment
     */
    public boolean isAssignment() {
        return LessonType.ASSIGNMENT.equals(this.lessonType);
    }

    /**
     * Checks if this lesson is a live session
     */
    public boolean isLiveSession() {
        return LessonType.LIVE_SESSION.equals(this.lessonType);
    }

    /**
     * Increments the view count
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * Updates average completion time
     */
    public void updateAvgCompletionTime(int completionTimeMinutes) {
        if (this.avgCompletionTime == null) {
            this.avgCompletionTime = completionTimeMinutes;
        } else {
            // Simple moving average
            this.avgCompletionTime = (this.avgCompletionTime + completionTimeMinutes) / 2;
        }
    }

    /**
     * Adds metadata entry
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Gets metadata value
     */
    public Object getMetadataValue(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    /**
     * Checks if lesson has video content
     */
    public boolean hasVideoContent() {
        return this.videoUrl != null && !this.videoUrl.trim().isEmpty();
    }

    /**
     * Checks if lesson has article content
     */
    public boolean hasArticleContent() {
        return this.articleContent != null && !this.articleContent.trim().isEmpty();
    }

    /**
     * Gets estimated completion time
     */
    public int getEstimatedCompletionTime() {
        if (this.durationMinutes != null) {
            return this.durationMinutes;
        }
        if (this.avgCompletionTime != null) {
            return this.avgCompletionTime;
        }
        // Default estimates by type
        return switch (this.lessonType) {
            case VIDEO -> 15;
            case ARTICLE -> 10;
            case QUIZ -> 20;
            case ASSIGNMENT -> 60;
            case LIVE_SESSION -> 90;
        };
    }

    @PrePersist
    protected void onCreate() {
        if (this.isFreePreview == null) {
            this.isFreePreview = false;
        }
        if (this.isPublished == null) {
            this.isPublished = true;
        }
        if (this.isMandatory == null) {
            this.isMandatory = true;
        }
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
    }
}

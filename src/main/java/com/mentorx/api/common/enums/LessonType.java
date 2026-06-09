package com.mentorx.api.common.enums;

/**
 * Enum representing different types of course lessons
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum LessonType {
    /**
     * Standard lesson. May include rich text, optional video, and optional downloadable resource.
     */
    LESSON,
    
    /**
     * Quiz/assessment lesson
     */
    QUIZ,

    /**
     * Legacy values kept only so existing database rows can be read safely.
     * New writes normalize these values back to LESSON.
     */
    VIDEO,
    ARTICLE,
    TEXT,
    DOWNLOADABLE,
    ASSIGNMENT,
    LIVE_SESSION
}

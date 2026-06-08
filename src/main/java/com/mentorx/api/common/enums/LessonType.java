package com.mentorx.api.common.enums;

/**
 * Enum representing different types of course lessons
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum LessonType {
    /**
     * Video lesson (recorded video content)
     */
    VIDEO,
    
    /**
     * Article/text-based lesson
     */
    ARTICLE,

    /**
     * Downloadable protected course resource
     */
    DOWNLOADABLE,
    
    /**
     * Quiz/assessment lesson
     */
    QUIZ,
    
    /**
     * Assignment/homework lesson
     */
    ASSIGNMENT,
    
    /**
     * Live session/webinar lesson
     */
    LIVE_SESSION
}

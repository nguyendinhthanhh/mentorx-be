package com.mentorx.api.feature.moderation.enums;

/**
 * Enum representing different types of entities that can be reported
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum ReportTargetType {
    /**
     * Report against a user profile
     */
    USER_PROFILE,
    
    /**
     * Report against a job posting
     */
    JOB_POSTING,
    
    /**
     * Report against a course
     */
    COURSE,
    
    /**
     * Report against a review
     */
    REVIEW,
    
    /**
     * Report against a chat message
     */
    MESSAGE,
    
    /**
     * Report against a comment
     */
    COMMENT,
    
    /**
     * Report against a mentor profile
     */
    MENTOR_PROFILE,
    
    /**
     * Report against course content
     */
    COURSE_CONTENT,
    
    /**
     * Report against a contract
     */
    CONTRACT,
    
    /**
     * Report against platform behavior
     */
    PLATFORM_ISSUE
}
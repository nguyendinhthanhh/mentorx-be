package com.mentorx.api.feature.review.enums;

/**
 * Enum representing different types of entities that can be reviewed
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum ReviewTargetType {
    /**
     * Review for a mentor profile
     */
    MENTOR,
    
    /**
     * Review for a completed job/contract
     */
    JOB_CONTRACT,
    
    /**
     * Review for a course
     */
    COURSE,
    
    /**
     * Review for a client (from mentor's perspective)
     */
    CLIENT,
    
    /**
     * Review for a quick support session
     */
    QUICK_SUPPORT,
    
    /**
     * Review for platform service
     */
    PLATFORM_SERVICE
}
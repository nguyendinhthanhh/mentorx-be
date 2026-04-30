package com.mentorx.api.feature.job.enums;

/**
 * Enum representing budget types for jobs
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum BudgetType {
    /**
     * Fixed budget for the entire project
     */
    FIXED,
    
    /**
     * Hourly rate
     */
    HOURLY,
    
    /**
     * Budget range (min-max)
     */
    RANGE,
    
    /**
     * To be discussed/negotiated
     */
    NEGOTIABLE,
    
    /**
     * Per milestone payment
     */
    PER_MILESTONE
}
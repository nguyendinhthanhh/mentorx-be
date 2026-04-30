package com.mentorx.api.feature.matching.enums;

/**
 * Enum representing different types of feed items in the matching system
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum FeedItemType {
    /**
     * Recommended mentor based on user interests
     */
    MENTOR_RECOMMENDATION,
    
    /**
     * Job posting that matches user skills
     */
    JOB_RECOMMENDATION,
    
    /**
     * Course suggestion based on learning path
     */
    COURSE_RECOMMENDATION,
    
    /**
     * Trending content in user's categories
     */
    TRENDING_CONTENT,
    
    /**
     * Featured mentor slot (promoted content)
     */
    FEATURED_MENTOR,
    
    /**
     * Quick support opportunity
     */
    QUICK_SUPPORT,
    
    /**
     * Personalized learning path suggestion
     */
    LEARNING_PATH,
    
    /**
     * Community activity and updates
     */
    COMMUNITY_UPDATE
}
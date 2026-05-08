package com.mentorx.api.common.enums;

/**
 * Enum representing types of items that can appear in personalized feed
 * Used in precomputed_feed_items table for the Discovery Dashboard
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public enum FeedItemType {
    /**
     * Mentor recommendation - personalized mentor profile suggestions
     */
    MENTOR,
    
    /**
     * Course recommendation - personalized course suggestions
     */
    COURSE,
    
    /**
     * Knowledge content - articles, posts, tutorials matched to user interests
     */
    KNOWLEDGE,
    
    /**
     * Job recommendation - job postings matched to user skills
     */
    JOB
}

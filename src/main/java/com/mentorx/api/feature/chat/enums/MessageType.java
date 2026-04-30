package com.mentorx.api.feature.chat.enums;

/**
 * Enum representing different types of chat messages
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
public enum MessageType {
    /**
     * Regular text message
     */
    TEXT,
    
    /**
     * Image attachment
     */
    IMAGE,
    
    /**
     * File attachment
     */
    FILE,
    
    /**
     * Voice message
     */
    VOICE,
    
    /**
     * Video message
     */
    VIDEO,
    
    /**
     * Code snippet
     */
    CODE,
    
    /**
     * System notification message
     */
    SYSTEM,
    
    /**
     * Typing indicator
     */
    TYPING,
    
    /**
     * Read receipt
     */
    READ_RECEIPT,
    
    /**
     * Session start notification
     */
    SESSION_START,
    
    /**
     * Session end notification
     */
    SESSION_END,
    
    /**
     * Payment request
     */
    PAYMENT_REQUEST,
    
    /**
     * Contract proposal
     */
    CONTRACT_PROPOSAL
}
package com.mentorx.api.common.constants;

public final class AppConstants {
    
    // Pagination
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String MAX_PAGE_SIZE = "100";
    
    // Security
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER_STRING = "Authorization";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    
    // File Upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
    
    // Wallet
    public static final String WALLET_CURRENCY = "MXC";
    public static final int WALLET_DECIMAL_PLACES = 2;
    public static final long COOLING_OFF_PERIOD_HOURS = 72;
    
    // Matching
    public static final double SKILL_OVERLAP_WEIGHT = 0.35;
    public static final double CATEGORY_AFFINITY_WEIGHT = 0.25;
    public static final double QUALITY_WEIGHT = 0.20;
    public static final double BEHAVIORAL_WEIGHT = 0.12;
    public static final double BUDGET_FIT_WEIGHT = 0.08;
    
    // Cache Keys
    public static final String CACHE_USER_PREFIX = "user:";
    public static final String CACHE_MENTOR_PREFIX = "mentor:";
    public static final String CACHE_JOB_PREFIX = "job:";
    public static final String CACHE_COURSE_PREFIX = "course:";
    public static final String CACHE_MATCH_SCORE_PREFIX = "match_score:";
    
    // Rate Limiting
    public static final int LOGIN_ATTEMPTS_LIMIT = 5;
    public static final int LOGIN_LOCKOUT_DURATION_MINUTES = 15;
    
    private AppConstants() {
        // Utility class
    }
}
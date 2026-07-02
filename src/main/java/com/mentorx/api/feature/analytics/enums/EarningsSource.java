package com.mentorx.api.feature.analytics.enums;

/**
 * Earnings attribution source used by Phase 2 summary and Phase 4 course stats.
 * - {@code LONG_TERM_MENTORING}: subscription-style bookings via mentor packages
 * - {@code FREELANCE_PROJECT}: short-term freelance contracts (jobs)
 * - {@code COURSE_SALE}: course enrollments purchased by students
 */
public enum EarningsSource {
    LONG_TERM_MENTORING,
    FREELANCE_PROJECT,
    COURSE_SALE
}

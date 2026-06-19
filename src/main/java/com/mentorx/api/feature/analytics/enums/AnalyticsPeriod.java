package com.mentorx.api.feature.analytics.enums;

/**
 * Granularity for analytics rollup endpoints.
 * Per DEC-009 (noun form, matches {@code wallet_account_type} style).
 */
public enum AnalyticsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

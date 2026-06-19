package com.mentorx.api.feature.analytics.enums;

/**
 * Discriminator for the Job Statistics endpoint.
 * - {@code MENTOR}: aggregate proposals/contracts submitted by this user
 * - {@code CLIENT}: aggregate jobs posted by this user
 */
public enum JobStatsRole {
    MENTOR,
    CLIENT
}

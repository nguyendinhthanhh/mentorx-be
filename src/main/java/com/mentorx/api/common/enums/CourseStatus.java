package com.mentorx.api.common.enums;

public enum CourseStatus {
    DRAFT,           // Course is being created
    PENDING_REVIEW,  // Course is waiting for admin verification
    PUBLISHED,       // Course is live and visible
    REJECTED,        // Course review failed and needs mentor changes
    ARCHIVED         // Course is no longer active
}

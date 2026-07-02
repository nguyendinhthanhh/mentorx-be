package com.mentorx.api.feature.course.service;

import com.mentorx.api.feature.course.entity.CourseEnrollment;

import java.util.UUID;

public interface CertificateService {
    CourseEnrollment issueIfEligible(CourseEnrollment enrollment);
    byte[] renderCertificate(UUID enrollmentId);
}

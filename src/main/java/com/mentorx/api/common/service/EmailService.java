package com.mentorx.api.common.service;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async
    void sendVerificationEmail(String to, String displayName, String token, String baseUrl);

    @Async
    void sendPasswordResetEmail(String to, String recipientName, String resetUrl);

    @Async
    void sendMentorApprovalEmail(String to, String recipientName, String mentorDashboardUrl);

    @Async
    void sendMentorMoreInfoRequestEmail(String to, String recipientName, String reason, String updateUrl);
}

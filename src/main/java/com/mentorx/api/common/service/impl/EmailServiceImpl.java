package com.mentorx.api.common.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mentorx.api.common.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendVerificationEmail(String to, String displayName, String token, String baseUrl) {
        System.out.println("Email address: " + fromEmail);
        String subject = "Verify your email — MentorX";
        String verificationLink = baseUrl + "/verify-email?token=" + token;

        String html = buildVerificationTemplate(displayName, verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", to, e);
        }
    }

    private String buildVerificationTemplate(String name, String link) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verify your email</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f6f9;">
                        <tr>
                            <td align="center" style="padding:40px 16px;">
                                <table role="presentation" width="480" cellspacing="0" cellpadding="0" style="max-width:480px;width:100%%;">
                                    <!-- Header -->
                                    <tr>
                                        <td align="center" style="padding:32px 0 24px;background:linear-gradient(135deg,#075985,#0c4a6e);border-radius:16px 16px 0 0;">
                                            <span style="font-size:28px;font-weight:900;color:#ffffff;letter-spacing:-0.5px;">Mentor<span style="color:#818cf8;">X</span></span>
                                        </td>
                                    </tr>
                                    <!-- Body -->
                                    <tr>
                                        <td style="background:#ffffff;padding:40px 32px;border-radius:0 0 16px 16px;box-shadow:0 4px 24px rgba(0,0,0,0.06);">
                                            <h1 style="margin:0 0 8px;font-size:22px;font-weight:700;color:#1e293b;">Hi %s,</h1>
                                            <p style="margin:0 0 24px;font-size:15px;line-height:1.6;color:#64748b;">Thanks for signing up! Please verify your email address to get started with MentorX.</p>
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                                <tr>
                                                    <td align="center" style="padding:0 0 24px;">
                                                        <a href="%s" style="display:inline-block;padding:14px 40px;background-color:#4f46e5;color:#ffffff;font-size:15px;font-weight:600;text-decoration:none;border-radius:12px;box-shadow:0 4px 12px rgba(79,70,229,0.3);">Verify Email Address</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <p style="margin:0 0 4px;font-size:13px;color:#94a3b8;">Or copy this link into your browser:</p>
                                            <p style="margin:0 0 32px;font-size:13px;color:#4f46e5;word-break:break-all;"><a href="%s" style="color:#4f46e5;text-decoration:underline;">%s</a></p>
                                            <hr style="border:none;border-top:1px solid #e2e8f0;margin:0 0 24px;">
                                            <p style="margin:0;font-size:13px;color:#94a3b8;line-height:1.5;">This link expires in 24 hours. If you didn't create an account, you can safely ignore this email.</p>
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td align="center" style="padding:24px 0 0;">
                                            <p style="margin:0;font-size:12px;color:#94a3b8;">&copy; 2026 MentorX. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(name, link, link, link);
    }
}

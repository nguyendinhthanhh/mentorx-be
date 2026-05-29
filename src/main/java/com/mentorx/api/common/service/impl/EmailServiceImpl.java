package com.mentorx.api.common.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String recipientName, String resetUrl) {
        String subject = "Reset your MentorX password";
        String html = buildPasswordResetTemplate(recipientName, resetUrl);
        String plainText = buildPasswordResetText(recipientName, resetUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", to, e);
        }
    }

    private String buildPasswordResetText(String recipientName, String resetUrl) {
        String displayName = StringUtils.hasText(recipientName) ? recipientName.trim() : "there";
        return "Hi " + displayName + ",\n\n"
                + "We received a request to reset your MentorX password.\n\n"
                + "Use the secure link below to create a new password:\n"
                + resetUrl + "\n\n"
                + "This link expires in 1 hour. If you did not request this change, you can ignore this email.\n\n"
                + "MentorX Team";
    }

    private String buildPasswordResetTemplate(String recipientName, String resetUrl) {
        String safeName = escapeHtml(StringUtils.hasText(recipientName) ? recipientName.trim() : "there");
        String escapedResetUrl = escapeHtml(resetUrl);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Reset your password</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7fb;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:620px;background:#ffffff;border:1px solid #e2e8f0;border-radius:24px;overflow:hidden;box-shadow:0 18px 50px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="padding:24px 32px;background:linear-gradient(135deg,#0f172a 0%%,#312e81 58%%,#4f46e5 100%%);">
                              <div style="display:inline-flex;align-items:center;gap:12px;">
                                <div style="width:44px;height:44px;border-radius:14px;background:rgba(255,255,255,0.14);text-align:center;line-height:44px;font-size:22px;font-weight:700;color:#ffffff;">M</div>
                                <div>
                                  <div style="font-size:18px;font-weight:700;color:#ffffff;letter-spacing:-0.02em;">MentorX</div>
                                  <div style="font-size:12px;color:rgba(255,255,255,0.78);">Account security</div>
                                </div>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:36px 32px 12px 32px;">
                              <div style="display:inline-block;padding:6px 10px;border-radius:999px;background:#eef2ff;color:#4338ca;font-size:12px;font-weight:700;letter-spacing:0.03em;text-transform:uppercase;">Password reset</div>
                              <h1 style="margin:18px 0 12px;font-size:30px;line-height:1.15;letter-spacing:-0.03em;color:#0f172a;">Create a new password for your account</h1>
                              <p style="margin:0;font-size:16px;line-height:1.7;color:#475569;">
                                Hi <strong>%s</strong>, we received a request to reset your MentorX password. Use the secure button below to continue.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px 8px 32px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;">
                                <tr>
                                  <td style="padding:24px;">
                                    <div style="font-size:13px;font-weight:700;text-transform:uppercase;letter-spacing:0.04em;color:#6366f1;margin-bottom:10px;">Secure access</div>
                                    <p style="margin:0 0 20px;font-size:15px;line-height:1.7;color:#475569;">
                                      This reset link expires in <strong style="color:#0f172a;">1 hour</strong>. Resetting your password will sign out previous sessions.
                                    </p>
                                    <a href="%s" style="display:inline-block;padding:14px 22px;border-radius:14px;background:#111827;color:#ffffff;text-decoration:none;font-size:15px;font-weight:700;">
                                      Create a new password
                                    </a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 32px 10px 32px;">
                              <p style="margin:0 0 8px;font-size:13px;color:#64748b;">If the button does not work, copy this link into your browser:</p>
                              <p style="margin:0;word-break:break-all;font-size:13px;line-height:1.6;color:#312e81;">%s</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 32px 34px 32px;">
                              <p style="margin:0;font-size:13px;line-height:1.7;color:#64748b;">
                                If you did not request a password reset, you can ignore this email. Your password will remain unchanged.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, escapedResetUrl, escapedResetUrl);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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

    @Override
    @Async
    public void sendMentorApprovalEmail(String to, String recipientName, String mentorDashboardUrl) {
        String subject = "Congratulations! You are now a Mentor on MentorX";
        String html = buildMentorApprovalTemplate(recipientName, mentorDashboardUrl);
        String plainText = buildMentorApprovalText(recipientName, mentorDashboardUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
            log.info("Mentor approval email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send mentor approval email to {}", to, e);
        }
    }

    private String buildMentorApprovalText(String recipientName, String mentorDashboardUrl) {
        String displayName = StringUtils.hasText(recipientName) ? recipientName.trim() : "there";
        return "Hi " + displayName + ",\n\n"
                + "Congratulations! Your mentor application has been approved.\n\n"
                + "You can now access your mentor dashboard here:\n"
                + mentorDashboardUrl + "\n\n"
                + "Welcome to the MentorX community!\n\n"
                + "MentorX Team";
    }

    private String buildMentorApprovalTemplate(String recipientName, String mentorDashboardUrl) {
        String safeName = escapeHtml(StringUtils.hasText(recipientName) ? recipientName.trim() : "there");
        String safeDashboardUrl = escapeHtml(mentorDashboardUrl);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Mentor Application Approved</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7fb;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:620px;background:#ffffff;border:1px solid #e2e8f0;border-radius:24px;overflow:hidden;box-shadow:0 18px 50px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="padding:24px 32px;background:linear-gradient(135deg,#0f172a 0%%,#10b981 58%%,#059669 100%%);">
                              <div style="display:inline-flex;align-items:center;gap:12px;">
                                <div style="width:44px;height:44px;border-radius:14px;background:rgba(255,255,255,0.14);text-align:center;line-height:44px;font-size:22px;font-weight:700;color:#ffffff;">M</div>
                                <div>
                                  <div style="font-size:18px;font-weight:700;color:#ffffff;letter-spacing:-0.02em;">MentorX</div>
                                  <div style="font-size:12px;color:rgba(255,255,255,0.78);">Application Approved</div>
                                </div>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:36px 32px 12px 32px;">
                              <h1 style="margin:18px 0 12px;font-size:30px;line-height:1.15;letter-spacing:-0.03em;color:#0f172a;">Welcome to MentorX!</h1>
                              <p style="margin:0;font-size:16px;line-height:1.7;color:#475569;">
                                Hi <strong>%s</strong>, congratulations! Your mentor application has been approved. You are now an official mentor on MentorX.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px 8px 32px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;">
                                <tr>
                                  <td style="padding:24px;">
                                    <div style="font-size:13px;font-weight:700;text-transform:uppercase;letter-spacing:0.04em;color:#10b981;margin-bottom:10px;">Get Started</div>
                                    <p style="margin:0 0 20px;font-size:15px;line-height:1.7;color:#475569;">
                                      Head over to your mentor dashboard to set up your availability, update your profile, and start accepting mentees.
                                    </p>
                                    <a href="%s" style="display:inline-block;padding:14px 22px;border-radius:14px;background:#111827;color:#ffffff;text-decoration:none;font-size:15px;font-weight:700;">
                                      Go to Dashboard
                                    </a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, safeDashboardUrl);
    }

    @Override
    @Async
    public void sendMentorMoreInfoRequestEmail(String to, String recipientName, String reason, String updateUrl) {
        String subject = "Action Required: Update your MentorX Application";
        String html = buildMentorMoreInfoTemplate(recipientName, reason, updateUrl);
        String plainText = buildMentorMoreInfoText(recipientName, reason, updateUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
            log.info("Mentor more info request email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send mentor more info request email to {}", to, e);
        }
    }

    private String buildMentorMoreInfoText(String recipientName, String reason, String updateUrl) {
        String displayName = StringUtils.hasText(recipientName) ? recipientName.trim() : "there";
        return "Hi " + displayName + ",\n\n"
                + "Our moderation team has reviewed your mentor application and needs a little more information before we can approve it.\n\n"
                + "Moderator note:\n" + reason + "\n\n"
                + "Please update your profile here:\n"
                + updateUrl + "\n\n"
                + "MentorX Team";
    }

    private String buildMentorMoreInfoTemplate(String recipientName, String reason, String updateUrl) {
        String safeName = escapeHtml(StringUtils.hasText(recipientName) ? recipientName.trim() : "there");
        String safeReason = escapeHtml(reason != null ? reason : "Please review and update your application details.");
        String safeUpdateUrl = escapeHtml(updateUrl);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Application Update Required</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7fb;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:620px;background:#ffffff;border:1px solid #e2e8f0;border-radius:24px;overflow:hidden;box-shadow:0 18px 50px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="padding:24px 32px;background:linear-gradient(135deg,#0f172a 0%%,#f59e0b 58%%,#d97706 100%%);">
                              <div style="display:inline-flex;align-items:center;gap:12px;">
                                <div style="width:44px;height:44px;border-radius:14px;background:rgba(255,255,255,0.14);text-align:center;line-height:44px;font-size:22px;font-weight:700;color:#ffffff;">M</div>
                                <div>
                                  <div style="font-size:18px;font-weight:700;color:#ffffff;letter-spacing:-0.02em;">MentorX</div>
                                  <div style="font-size:12px;color:rgba(255,255,255,0.78);">Application Update Required</div>
                                </div>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:36px 32px 12px 32px;">
                              <h1 style="margin:18px 0 12px;font-size:30px;line-height:1.15;letter-spacing:-0.03em;color:#0f172a;">Action required on your application</h1>
                              <p style="margin:0;font-size:16px;line-height:1.7;color:#475569;">
                                Hi <strong>%s</strong>, our moderation team needs a little more information before we can approve your mentor application.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px 8px 32px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#fffbeb;border:1px solid #fde68a;border-radius:12px;">
                                <tr>
                                  <td style="padding:16px;">
                                    <div style="font-size:13px;font-weight:700;color:#d97706;margin-bottom:8px;">Moderator Note</div>
                                    <p style="margin:0;font-size:14px;line-height:1.6;color:#92400e;">
                                      %s
                                    </p>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 32px 24px 32px;">
                              <a href="%s" style="display:inline-block;padding:14px 22px;border-radius:14px;background:#111827;color:#ffffff;text-decoration:none;font-size:15px;font-weight:700;">
                                Update your application
                              </a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 32px 32px 32px;">
                              <p style="margin:0;font-size:13px;line-height:1.7;color:#64748b;">
                                If you have any questions, reply directly to this email to contact MentorX support.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, safeReason, safeUpdateUrl);
    }
}

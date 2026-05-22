package com.mentorx.api.auth.service.serviceImpl;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PasswordResetEmailDispatcher {

    private final JavaMailSender mailSender;
    private final String mailUsername;
    private final String mailPassword;
    private final String configuredFromEmail;

    public PasswordResetEmailDispatcher(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${app.mail.from:}") String configuredFromEmail
    ) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.configuredFromEmail = configuredFromEmail;
    }

    public void validateConfiguration() {
        resolveMailFromAddress();
    }

    @Async("mailTaskExecutor")
    public void sendPasswordResetEmailAsync(String toEmail, String recipientName, String resetUrl) {
        try {
            String fromAddress = resolveMailFromAddress();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Reset your MentorX password");
            helper.setText(
                    buildPasswordResetEmailText(recipientName, resetUrl),
                    buildPasswordResetEmailHtml(recipientName, resetUrl)
            );
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MailAuthenticationException ex) {
            log.error("SMTP authentication failed while sending password reset email to {}. Reset link: {}", toEmail, resetUrl, ex);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}. Reset link: {}", toEmail, resetUrl, ex);
        }
    }

    private String buildPasswordResetEmailText(String recipientName, String resetUrl) {
        String displayName = StringUtils.hasText(recipientName) ? recipientName.trim() : "there";
        return "Hi " + displayName + ",\n\n"
                + "We received a request to reset your MentorX password.\n\n"
                + "Use the secure link below to create a new password:\n"
                + resetUrl + "\n\n"
                + "This link expires in 1 hour. If you did not request this change, you can ignore this email.\n\n"
                + "MentorX Team";
    }

    private String buildPasswordResetEmailHtml(String recipientName, String resetUrl) {
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

    private String resolveMailFromAddress() {
        String resolvedUsername = normalizeMailCredential(mailUsername);
        String resolvedPassword = normalizeMailCredential(mailPassword);

        if (!StringUtils.hasText(resolvedUsername) || !StringUtils.hasText(resolvedPassword)) {
            throw new IllegalStateException("Email service is not configured. Missing SMTP username or password.");
        }

        String sender = StringUtils.hasText(configuredFromEmail) ? configuredFromEmail.trim() : resolvedUsername;
        try {
            InternetAddress address = new InternetAddress(sender, true);
            address.validate();
            return address.getAddress();
        } catch (Exception ex) {
            throw new IllegalStateException("Email sender address is invalid.", ex);
        }
    }

    private String normalizeMailCredential(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String normalized = value.trim();
        if (normalized.length() >= 2) {
            boolean wrappedInDoubleQuotes = normalized.startsWith("\"") && normalized.endsWith("\"");
            boolean wrappedInSingleQuotes = normalized.startsWith("'") && normalized.endsWith("'");
            if (wrappedInDoubleQuotes || wrappedInSingleQuotes) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }
        return normalized;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

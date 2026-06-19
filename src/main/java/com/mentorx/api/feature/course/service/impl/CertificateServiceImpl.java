package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.enums.CourseProductType;
import com.mentorx.api.feature.course.entity.CourseEnrollment;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateServiceImpl implements CertificateService {

    private final CourseEnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public CourseEnrollment issueIfEligible(CourseEnrollment enrollment) {
        if (enrollment.getCourse().getProductType() != CourseProductType.COURSE
                || !Boolean.TRUE.equals(enrollment.getIsCompleted())) {
            return enrollment;
        }
        if (enrollment.getCertificateCode() == null || enrollment.getCertificateCode().isBlank()) {
            String code = "MX-" + enrollment.getId().toString().substring(0, 8).toUpperCase();
            enrollment.setCertificateCode(code);
            enrollment.setCertificateIssuedAt(LocalDateTime.now());
            enrollment.setCertificateUrl("/api/v1/course-enrollments/" + enrollment.getId() + "/certificate");
            return enrollmentRepository.save(enrollment);
        }
        return enrollment;
    }

    @Override
    public byte[] renderCertificate(UUID enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        if (!Boolean.TRUE.equals(enrollment.getIsCompleted())) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Course is not completed");
        }
        CourseEnrollment issued = issueIfEligible(enrollment);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.LETTER.getHeight(), PDRectangle.LETTER.getWidth()));
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawCertificateBackground(content, page.getMediaBox());
                drawMedalIcon(content, 396, 430);

                content.setNonStrokingColor(79, 70, 229);
                writeCentered(content, "MENTOR X", PDType1Font.HELVETICA_BOLD, 16, 530);

                content.setNonStrokingColor(15, 23, 42);
                writeCentered(content, "CERTIFICATE OF COMPLETION", PDType1Font.HELVETICA_BOLD, 34, 486);

                content.setNonStrokingColor(100, 116, 139);
                writeCentered(content, "This certifies that", PDType1Font.HELVETICA, 14, 366);

                content.setNonStrokingColor(15, 23, 42);
                writeCentered(content, safeText(issued.getStudent().getFullName()), PDType1Font.HELVETICA_BOLD, 30, 322);
                drawRule(content, 236, 307, 556, 307, 226, 232, 240);

                content.setNonStrokingColor(100, 116, 139);
                writeCentered(content, "has successfully completed", PDType1Font.HELVETICA, 14, 278);

                content.setNonStrokingColor(79, 70, 229);
                writeWrappedCentered(content, safeText(issued.getCourse().getTitle()), PDType1Font.HELVETICA_BOLD, 22, 238, 560, 28);

                String date = issued.getCertificateIssuedAt().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
                content.setNonStrokingColor(51, 65, 85);
                writeCentered(content, "Issued " + date, PDType1Font.HELVETICA, 12, 150);
                writeCentered(content, "Certificate ID: " + issued.getCertificateCode(), PDType1Font.HELVETICA_BOLD, 12, 130);

                drawRule(content, 96, 112, 246, 112, 148, 163, 184);
                drawRule(content, 546, 112, 696, 112, 148, 163, 184);
                content.setNonStrokingColor(15, 23, 42);
                writeCenteredIn(content, "Mentor X", PDType1Font.HELVETICA_BOLD, 12, 96, 246, 92);
                writeCenteredIn(content, "Verified Credential", PDType1Font.HELVETICA_BOLD, 12, 546, 696, 92);
                content.setNonStrokingColor(100, 116, 139);
                writeCenteredIn(content, "Platform signature", PDType1Font.HELVETICA, 9, 96, 246, 76);
                writeCenteredIn(content, "mentorx.app", PDType1Font.HELVETICA, 9, 546, 696, 76);
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to render certificate", ex);
        }
    }

    private void drawCertificateBackground(PDPageContentStream content, PDRectangle page) throws IOException {
        content.setNonStrokingColor(248, 250, 252);
        content.addRect(0, 0, page.getWidth(), page.getHeight());
        content.fill();

        content.setStrokingColor(79, 70, 229);
        content.setLineWidth(4);
        content.addRect(28, 28, page.getWidth() - 56, page.getHeight() - 56);
        content.stroke();

        content.setStrokingColor(245, 158, 11);
        content.setLineWidth(1.5f);
        content.addRect(42, 42, page.getWidth() - 84, page.getHeight() - 84);
        content.stroke();

        content.setNonStrokingColor(238, 242, 255);
        content.addRect(54, 54, page.getWidth() - 108, page.getHeight() - 108);
        content.fill();

        content.setNonStrokingColor(255, 255, 255);
        content.addRect(70, 70, page.getWidth() - 140, page.getHeight() - 140);
        content.fill();
    }

    private void drawMedalIcon(PDPageContentStream content, float centerX, float centerY) throws IOException {
        content.setNonStrokingColor(245, 158, 11);
        drawCircle(content, centerX, centerY, 34);
        content.fill();

        content.setNonStrokingColor(255, 255, 255);
        drawCircle(content, centerX, centerY, 23);
        content.fill();

        content.setNonStrokingColor(79, 70, 229);
        writeCenteredIn(content, "MX", PDType1Font.HELVETICA_BOLD, 18, centerX - 34, centerX + 34, centerY - 6);
    }

    private void drawRule(
            PDPageContentStream content,
            float startX,
            float startY,
            float endX,
            float endY,
            int red,
            int green,
            int blue
    ) throws IOException {
        content.setStrokingColor(red, green, blue);
        content.setLineWidth(1);
        content.moveTo(startX, startY);
        content.lineTo(endX, endY);
        content.stroke();
    }

    private void drawCircle(PDPageContentStream content, float centerX, float centerY, float radius) throws IOException {
        float k = 0.552284749831f;
        float control = radius * k;
        content.moveTo(centerX + radius, centerY);
        content.curveTo(centerX + radius, centerY + control, centerX + control, centerY + radius, centerX, centerY + radius);
        content.curveTo(centerX - control, centerY + radius, centerX - radius, centerY + control, centerX - radius, centerY);
        content.curveTo(centerX - radius, centerY - control, centerX - control, centerY - radius, centerX, centerY - radius);
        content.curveTo(centerX + control, centerY - radius, centerX + radius, centerY - control, centerX + radius, centerY);
        content.closePath();
    }

    private void writeCentered(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            float fontSize,
            float y
    ) throws IOException {
        float pageWidth = PDRectangle.LETTER.getHeight();
        writeCenteredIn(content, text, font, fontSize, 0, pageWidth, y);
    }

    private void writeCenteredIn(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            float fontSize,
            float startX,
            float endX,
            float y
    ) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(startX + ((endX - startX) - textWidth) / 2, y);
        content.showText(text);
        content.endText();
    }

    private void writeWrappedCentered(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            float fontSize,
            float y,
            float maxWidth,
            float lineHeight
    ) throws IOException {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        float currentY = y;
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.getStringWidth(candidate) / 1000f * fontSize > maxWidth && !line.isEmpty()) {
                writeCentered(content, line.toString(), font, fontSize, currentY);
                currentY -= lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            writeCentered(content, line.toString(), font, fontSize, currentY);
        }
    }

    private String safeText(String text) {
        return text == null ? "" : text.replaceAll("[^\\x20-\\x7E]", "");
    }
}

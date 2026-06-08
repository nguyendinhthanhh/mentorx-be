package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
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
        if (!Boolean.TRUE.equals(enrollment.getCourse().getIsCertificate())
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
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setFont(PDType1Font.HELVETICA_BOLD, 28);
                writeCentered(content, "Certificate of Completion", 560);
                content.setFont(PDType1Font.HELVETICA, 16);
                writeCentered(content, "This certifies that", 500);
                content.setFont(PDType1Font.HELVETICA_BOLD, 24);
                writeCentered(content, issued.getStudent().getFullName(), 460);
                content.setFont(PDType1Font.HELVETICA, 16);
                writeCentered(content, "completed", 420);
                content.setFont(PDType1Font.HELVETICA_BOLD, 22);
                writeCentered(content, issued.getCourse().getTitle(), 385);
                content.setFont(PDType1Font.HELVETICA, 12);
                String date = issued.getCertificateIssuedAt().format(DateTimeFormatter.ISO_LOCAL_DATE);
                writeCentered(content, "Issued " + date + " | Certificate " + issued.getCertificateCode(), 330);
                writeCentered(content, "MentorX", 285);
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to render certificate", ex);
        }
    }

    private void writeCentered(PDPageContentStream content, String text, float y) throws IOException {
        float pageWidth = PDRectangle.LETTER.getWidth();
        float approximateWidth = text.length() * 6f;
        content.beginText();
        content.newLineAtOffset((pageWidth - approximateWidth) / 2, y);
        content.showText(text);
        content.endText();
    }
}

package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseDownloadAudit;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseDownloadAuditRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.service.CourseDocumentPayload;
import com.mentorx.api.feature.course.service.CourseDocumentService;
import com.mentorx.api.feature.system.config.FileStorageProperties;
import com.mentorx.api.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseDocumentServiceImpl implements CourseDocumentService {

    private static final int PREVIEW_PAGE_LIMIT = 2;
    private static final float WATERMARK_FONT_SIZE = 48f;
    private static final float WATERMARK_OPACITY = 0.15f;

    private final CourseLessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseDownloadAuditRepository downloadAuditRepository;
    private final FileStorageProperties fileStorageProperties;

    @Override
    public CourseDocumentPayload getPreview(UUID lessonId, User viewer) {
        CourseLesson lesson = requireLesson(lessonId);
        Course course = lesson.getSection().getCourse();

        boolean isPaidCourse = course.getPriceMxc() != null && course.getPriceMxc().compareTo(BigDecimal.ZERO) > 0;
        boolean isEnrolled = viewer != null && enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), viewer.getId());
        boolean previewOnly = isPaidCourse && !isEnrolled;

        return buildDocumentPayload(lesson, course, viewer, previewOnly);
    }

    @Override
    public CourseDocumentPayload getDownload(UUID lessonId, User viewer, String ipAddress, String userAgent) {
        if (viewer == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        CourseLesson lesson = requireLesson(lessonId);
        Course course = lesson.getSection().getCourse();

        boolean isPaidCourse = course.getPriceMxc() != null && course.getPriceMxc().compareTo(BigDecimal.ZERO) > 0;
        boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), viewer.getId());

        if (isPaidCourse && !isEnrolled) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        CourseDocumentPayload payload = buildDocumentPayload(lesson, course, viewer, false);
        downloadAuditRepository.save(CourseDownloadAudit.builder()
                .course(course)
                .lesson(lesson)
                .user(viewer)
                .fileUrl(lesson.getResourceUrl())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .downloadedAt(LocalDateTime.now())
                .build());
        return payload;
    }

    private CourseLesson requireLesson(UUID lessonId) {
        CourseLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getIsPublished() == null || !lesson.getIsPublished()) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Course course = lesson.getSection().getCourse();
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        return lesson;
    }

    private CourseDocumentPayload buildDocumentPayload(CourseLesson lesson, Course course, User viewer, boolean previewOnly) {
        Path filePath = resolveDocumentPath(lesson.getResourceUrl());
        String watermark = buildWatermarkText(viewer);
        String fileName = buildFileName(course.getTitle(), lesson.getTitle());

        try (PDDocument source = PDDocument.load(filePath.toFile())) {
            PDDocument target = previewOnly ? new PDDocument() : source;

            if (previewOnly) {
                int limit = Math.min(PREVIEW_PAGE_LIMIT, source.getNumberOfPages());
                for (int i = 0; i < limit; i++) {
                    target.importPage(source.getPage(i));
                }
            }

            applyWatermark(target, watermark);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            target.save(outputStream);

            if (previewOnly) {
                target.close();
            }

            return new CourseDocumentPayload(outputStream.toByteArray(), fileName);
        } catch (IOException ex) {
            log.error("Failed to generate document for lesson {}", lesson.getId(), ex);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to render document", ex);
        }
    }

    private void applyWatermark(PDDocument document, String watermarkText) throws IOException {
        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant(WATERMARK_OPACITY);

        for (PDPage page : document.getPages()) {
            PDRectangle mediaBox = page.getMediaBox();
            float centerX = mediaBox.getWidth() / 2;
            float centerY = mediaBox.getHeight() / 2;

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
            )) {
                contentStream.setGraphicsStateParameters(graphicsState);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, WATERMARK_FONT_SIZE);
                contentStream.setNonStrokingColor(150, 150, 150);
                contentStream.beginText();
                contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), centerX, centerY));
                contentStream.showText(watermarkText);
                contentStream.endText();
            }
        }
    }

    private Path resolveDocumentPath(String resourceUrl) {
        if (resourceUrl == null || resourceUrl.isBlank()) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND, "Document resource not found");
        }

        String fileName = resourceUrl;
        int uploadsIndex = resourceUrl.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            fileName = resourceUrl.substring(uploadsIndex + "/uploads/".length());
        }

        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash >= 0) {
            fileName = fileName.substring(lastSlash + 1);
        }

        int queryIndex = fileName.indexOf('?');
        if (queryIndex >= 0) {
            fileName = fileName.substring(0, queryIndex);
        }
        int hashIndex = fileName.indexOf('#');
        if (hashIndex >= 0) {
            fileName = fileName.substring(0, hashIndex);
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE, "Only PDF documents are supported");
        }

        Path uploadDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Invalid document path");
        }

        if (!Files.exists(filePath)) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND, "Document file not found");
        }

        return filePath;
    }

    private String buildWatermarkText(User viewer) {
        if (viewer == null) {
            return "MentorX Preview";
        }
        return "MentorX - " + viewer.getEmail() + " - " + LocalDateTime.now();
    }

    private String buildFileName(String courseTitle, String lessonTitle) {
        String baseName = (courseTitle + "-" + lessonTitle).trim();
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9\\-_.]", "-");
        String collapsed = sanitized.replaceAll("-{2,}", "-");
        if (collapsed.isBlank()) {
            return "mentorx-document.pdf";
        }
        return collapsed + ".pdf";
    }
}

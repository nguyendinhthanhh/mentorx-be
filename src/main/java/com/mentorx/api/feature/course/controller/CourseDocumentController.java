package com.mentorx.api.feature.course.controller;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.service.CourseDocumentPayload;
import com.mentorx.api.feature.course.service.CourseDocumentService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/course-documents")
@RequiredArgsConstructor
public class CourseDocumentController {

    private final CourseDocumentService documentService;
    private final UserRepository userRepository;

    @GetMapping("/lessons/{lessonId}/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> previewDocument(@PathVariable UUID lessonId, Authentication authentication) {
        User viewer = resolveCurrentUser(authentication);
        CourseDocumentPayload payload = documentService.getPreview(lessonId, viewer);
        return buildPdfResponse(payload, false);
    }

    @GetMapping("/lessons/{lessonId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable UUID lessonId,
            Authentication authentication,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        User viewer = resolveCurrentUser(authentication);
        CourseDocumentPayload payload = documentService.getDownload(lessonId, viewer, forwardedFor, userAgent);
        return buildPdfResponse(payload, true);
    }

    private ResponseEntity<byte[]> buildPdfResponse(CourseDocumentPayload payload, boolean download) {
        String dispositionType = download ? "attachment" : "inline";
        String contentDisposition = String.format("%s; filename=\"%s\"", dispositionType, payload.fileName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .body(payload.content());
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}

package com.mentorx.api.feature.course.controller;

import com.mentorx.api.feature.course.dto.request.LessonCommentCreateRequest;
import com.mentorx.api.feature.course.dto.request.LessonCommentUpdateRequest;
import com.mentorx.api.feature.course.dto.response.LessonCommentResponse;
import com.mentorx.api.feature.course.service.LessonCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lesson-comments")
@RequiredArgsConstructor
public class LessonCommentController {

    private final LessonCommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonCommentResponse> createComment(
            @Valid @RequestBody LessonCommentCreateRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        LessonCommentResponse response = commentService.createComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonCommentResponse> getCommentById(@PathVariable UUID id) {
        LessonCommentResponse response = commentService.getCommentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<Page<LessonCommentResponse>> getCommentsByLessonId(
            @PathVariable UUID lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<LessonCommentResponse> responses = commentService.getCommentsByLessonId(lessonId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/parent/{parentId}/replies")
    public ResponseEntity<List<LessonCommentResponse>> getRepliesByParentId(@PathVariable UUID parentId) {
        List<LessonCommentResponse> responses = commentService.getRepliesByParentId(parentId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LessonCommentResponse>> getCommentsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<LessonCommentResponse> responses = commentService.getCommentsByUserId(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/lesson/{lessonId}/count")
    public ResponseEntity<Long> countCommentsByLessonId(@PathVariable UUID lessonId) {
        Long count = commentService.countCommentsByLessonId(lessonId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonCommentResponse> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody LessonCommentUpdateRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        LessonCommentResponse response = commentService.updateComment(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}

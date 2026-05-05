package com.mentorx.api.feature.review.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.review.dto.request.ReviewReportCreateRequest;
import com.mentorx.api.feature.review.dto.response.ReviewReportResponse;
import com.mentorx.api.feature.review.service.ReviewReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/review-reports")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @PostMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewReportResponse>> createReport(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(reviewReportService.reportReview(reviewId, request)));
    }

    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReviewReportResponse>> resolve(
            @PathVariable UUID reportId,
            @RequestParam UUID adminId,
            @RequestParam String action,
            @RequestParam String notes,
            @RequestParam boolean isUpheld) {
        return ResponseEntity.ok(ApiResponse.success(reviewReportService.resolveReport(reportId, adminId, action, notes, isUpheld)));
    }

    @PostMapping("/{reportId}/dismiss")
    public ResponseEntity<ApiResponse<ReviewReportResponse>> dismiss(
            @PathVariable UUID reportId,
            @RequestParam UUID adminId,
            @RequestParam String notes) {
        return ResponseEntity.ok(ApiResponse.success(reviewReportService.dismissReport(reportId, adminId, notes)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewReportResponse>>> getByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewReportService.getReportsByStatus(status, PageRequest.of(page, size))));
    }
}

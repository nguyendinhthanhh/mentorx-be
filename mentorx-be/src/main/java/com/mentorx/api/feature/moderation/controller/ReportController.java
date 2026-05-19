package com.mentorx.api.feature.moderation.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.moderation.dto.request.ReportCreateRequest;
import com.mentorx.api.feature.moderation.dto.request.ReportResolveRequest;
import com.mentorx.api.feature.moderation.dto.response.ReportResponse;
import com.mentorx.api.feature.moderation.enums.ReportStatus;
import com.mentorx.api.feature.moderation.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(@Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(reportService.createReport(request)));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable UUID reportId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportById(reportId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReportsByStatus(
            @RequestParam ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportsByStatus(status, PageRequest.of(page, size))));
    }

    @PostMapping("/{reportId}/assign")
    public ResponseEntity<ApiResponse<ReportResponse>> assignReport(
            @PathVariable UUID reportId,
            @RequestParam UUID adminId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.assignReport(reportId, adminId)));
    }

    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable UUID reportId,
            @Valid @RequestBody ReportResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reportService.resolveReport(reportId, request)));
    }

    @PostMapping("/{reportId}/escalate")
    public ResponseEntity<ApiResponse<ReportResponse>> escalateReport(
            @PathVariable UUID reportId,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(reportService.escalateReport(reportId, reason)));
    }
}

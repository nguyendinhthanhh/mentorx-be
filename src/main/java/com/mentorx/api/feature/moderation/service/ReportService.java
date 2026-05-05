package com.mentorx.api.feature.moderation.service;

import com.mentorx.api.feature.moderation.dto.request.ReportCreateRequest;
import com.mentorx.api.feature.moderation.dto.request.ReportResolveRequest;
import com.mentorx.api.feature.moderation.dto.response.ReportResponse;
import com.mentorx.api.feature.moderation.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportService {
    ReportResponse createReport(ReportCreateRequest request);
    ReportResponse getReportById(UUID reportId);
    Page<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable);
    ReportResponse assignReport(UUID reportId, UUID adminId);
    ReportResponse resolveReport(UUID reportId, ReportResolveRequest request);
    ReportResponse escalateReport(UUID reportId, String reason);
}

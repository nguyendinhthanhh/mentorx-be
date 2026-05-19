package com.mentorx.api.feature.moderation.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.moderation.dto.request.ReportCreateRequest;
import com.mentorx.api.feature.moderation.dto.request.ReportResolveRequest;
import com.mentorx.api.feature.moderation.dto.response.ReportResponse;
import com.mentorx.api.feature.moderation.entity.Report;
import com.mentorx.api.feature.moderation.enums.ReportStatus;
import com.mentorx.api.feature.moderation.repository.ReportRepository;
import com.mentorx.api.feature.moderation.service.ReportService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReportResponse createReport(ReportCreateRequest request) {
        User reporter = userRepository.findById(request.reporterId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        User reportedUser = null;
        if (request.reportedUserId() != null) {
            reportedUser = userRepository.findById(request.reportedUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReportedUser(reportedUser);
        report.setReportCategory(request.reportCategory());
        report.setReason(request.reason());
        report.setReportContext(request.reportContext());
        
        if (request.evidenceUrls() != null && !request.evidenceUrls().isEmpty()) {
            Map<String, Object> evidenceUrls = new HashMap<>();
            evidenceUrls.put("urls", request.evidenceUrls());
            report.setEvidenceUrls(evidenceUrls);
        }

        return toResponse(reportRepository.save(report));
    }

    @Override
    public ReportResponse getReportById(UUID reportId) {
        return toResponse(findReport(reportId));
    }

    @Override
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ReportResponse assignReport(UUID reportId, UUID adminId) {
        Report report = findReport(reportId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        report.assignTo(admin);
        return toResponse(reportRepository.save(report));
    }

    @Override
    @Transactional
    public ReportResponse resolveReport(UUID reportId, ReportResolveRequest request) {
        Report report = findReport(reportId);
        report.resolve(request.actionTaken(), request.moderatorNotes(), request.isUpheld());
        return toResponse(reportRepository.save(report));
    }

    @Override
    @Transactional
    public ReportResponse escalateReport(UUID reportId, String reason) {
        Report report = findReport(reportId);
        report.escalate(reason);
        return toResponse(reportRepository.save(report));
    }

    private Report findReport(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    private ReportResponse toResponse(Report report) {
        java.util.List<String> evidenceUrlList = null;
        if (report.getEvidenceUrls() != null && report.getEvidenceUrls().containsKey("urls")) {
            evidenceUrlList = (java.util.List<String>) report.getEvidenceUrls().get("urls");
        }

        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getFullName(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReportedUser() != null ? report.getReportedUser().getId() : null,
                report.getReportedUser() != null ? report.getReportedUser().getFullName() : null,
                report.getReportCategory(),
                report.getReason(),
                report.getStatus(),
                report.getPriorityLevel(),
                report.getAssignedToAdmin() != null ? report.getAssignedToAdmin().getId() : null,
                report.getAssignedAt(),
                report.getReviewedAt(),
                report.getResolvedAt(),
                report.getActionTaken(),
                report.getModeratorNotes(),
                report.getIsUpheld(),
                report.getIsDuplicate(),
                report.getOriginalReportId(),
                report.getSimilarReportCount(),
                report.getIsUrgent(),
                report.getContentHidden(),
                report.getContentHiddenAt(),
                evidenceUrlList,
                report.getReportContext(),
                report.getEscalationLevel(),
                report.getEscalatedAt(),
                report.getEscalationReason(),
                report.getSlaDeadline(),
                report.getSlaMet(),
                report.getResolutionTimeHours(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}

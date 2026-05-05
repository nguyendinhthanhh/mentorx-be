package com.mentorx.api.feature.review.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.review.dto.request.ReviewReportCreateRequest;
import com.mentorx.api.feature.review.dto.response.ReviewReportResponse;
import com.mentorx.api.feature.review.entity.Review;
import com.mentorx.api.feature.review.entity.ReviewReport;
import com.mentorx.api.feature.review.repository.ReviewReportRepository;
import com.mentorx.api.feature.review.repository.ReviewRepository;
import com.mentorx.api.feature.review.service.ReviewReportService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReportServiceImpl implements ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewReportResponse reportReview(UUID reviewId, ReviewReportCreateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        User reporter = userRepository.findById(request.reporterId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporter(reporter);
        report.setReportReason(request.reportReason());
        report.setDescription(request.description());
        report.setStatus("PENDING");

        ReviewReport savedReport = reviewReportRepository.save(report);

        review.setReportCount(review.getReportCount() + 1);
        reviewRepository.save(review);

        return toResponse(savedReport);
    }

    @Override
    @Transactional
    public ReviewReportResponse resolveReport(UUID reportId, UUID adminId, String action, String notes, boolean isUpheld) {
        ReviewReport report = findReport(reportId);
        report.resolve(adminId, action, notes, isUpheld);
        
        Review review = report.getReview();
        review.setIsModerated(true);
        review.setModeratedAt(java.time.LocalDateTime.now());
        review.setModeratedByAdminId(adminId);
        review.setModerationNotes(notes);
        
        if ("REVIEW_HIDDEN".equals(action) || "REVIEW_REMOVED".equals(action)) {
            review.setIsHidden(true);
            review.setHiddenReason(action);
        }
        reviewRepository.save(review);
        
        return toResponse(reviewReportRepository.save(report));
    }

    @Override
    @Transactional
    public ReviewReportResponse dismissReport(UUID reportId, UUID adminId, String notes) {
        ReviewReport report = findReport(reportId);
        report.dismiss(adminId, notes);
        
        Review review = report.getReview();
        review.setIsModerated(true);
        review.setModeratedAt(java.time.LocalDateTime.now());
        review.setModeratedByAdminId(adminId);
        review.setModerationNotes("Report dismissed: " + notes);
        reviewRepository.save(review);
        
        return toResponse(reviewReportRepository.save(report));
    }

    @Override
    public Page<ReviewReportResponse> getReportsByStatus(String status, Pageable pageable) {
        return reviewReportRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    private ReviewReport findReport(UUID reportId) {
        return reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_REPORT_NOT_FOUND));
    }

    private ReviewReportResponse toResponse(ReviewReport report) {
        return new ReviewReportResponse(
                report.getId(),
                report.getReview().getId(),
                report.getReporter().getId(),
                report.getReporter().getFullName(),
                report.getReportReason(),
                report.getDescription(),
                report.getStatus(),
                report.getReviewedAt(),
                report.getReviewedByAdminId(),
                report.getActionTaken(),
                report.getReviewNotes(),
                report.getResolvedAt(),
                report.getIsUpheld(),
                report.getPriorityLevel(),
                report.getIsDuplicate(),
                report.getOriginalReportId(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}

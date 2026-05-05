package com.mentorx.api.feature.review.service;

import com.mentorx.api.feature.review.dto.request.ReviewReportCreateRequest;
import com.mentorx.api.feature.review.dto.response.ReviewReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewReportService {
    ReviewReportResponse reportReview(UUID reviewId, ReviewReportCreateRequest request);
    ReviewReportResponse resolveReport(UUID reportId, UUID adminId, String action, String notes, boolean isUpheld);
    ReviewReportResponse dismissReport(UUID reportId, UUID adminId, String notes);
    Page<ReviewReportResponse> getReportsByStatus(String status, Pageable pageable);
}

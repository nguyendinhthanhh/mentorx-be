package com.mentorx.api.feature.review.repository;

import com.mentorx.api.feature.review.entity.ReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, UUID> {
    Page<ReviewReport> findByReviewId(UUID reviewId, Pageable pageable);
    Page<ReviewReport> findByStatus(String status, Pageable pageable);
}

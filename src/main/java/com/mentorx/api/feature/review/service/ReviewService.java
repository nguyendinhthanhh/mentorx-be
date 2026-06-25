package com.mentorx.api.feature.review.service;

import com.mentorx.api.feature.review.dto.request.ReviewCreateRequest;
import com.mentorx.api.feature.review.dto.request.ReviewResponseRequest;
import com.mentorx.api.feature.review.dto.request.ReviewUpdateRequest;
import com.mentorx.api.feature.review.dto.response.ReviewResponse;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponse createReview(UUID currentUserId, ReviewCreateRequest request);
    ReviewResponse updateReview(UUID currentUserId, UUID reviewId, ReviewUpdateRequest request);
    ReviewResponse respondToReview(UUID currentUserId, UUID reviewId, ReviewResponseRequest request);
    ReviewResponse getReviewById(UUID reviewId);
    Page<ReviewResponse> getReviewsByTarget(ReviewTargetType targetType, UUID targetId, Pageable pageable);
    Page<ReviewResponse> getReviewsByReviewer(UUID reviewerId, Pageable pageable);
    boolean canReviewMentor(UUID currentUserId, UUID mentorId);
    ReviewResponse voteHelpful(UUID currentUserId, UUID reviewId, boolean isHelpful);
    void deleteReview(UUID currentUserId, UUID reviewId);
}

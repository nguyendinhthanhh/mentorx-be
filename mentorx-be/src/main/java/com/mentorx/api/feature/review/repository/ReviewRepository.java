package com.mentorx.api.feature.review.repository;

import com.mentorx.api.feature.review.entity.Review;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByTargetTypeAndTargetIdAndIsHiddenFalseAndIsPublicTrue(ReviewTargetType targetType, UUID targetId, Pageable pageable);
    Page<Review> findByReviewerId(UUID reviewerId, Pageable pageable);
    Optional<Review> findByReviewerIdAndTargetTypeAndTargetId(UUID reviewerId, ReviewTargetType targetType, UUID targetId);
}

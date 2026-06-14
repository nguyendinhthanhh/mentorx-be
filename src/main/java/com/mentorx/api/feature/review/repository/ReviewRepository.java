package com.mentorx.api.feature.review.repository;

import com.mentorx.api.feature.review.entity.Review;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByTargetTypeAndTargetIdAndIsHiddenFalseAndIsPublicTrue(ReviewTargetType targetType, UUID targetId, Pageable pageable);
    Page<Review> findByReviewerId(UUID reviewerId, Pageable pageable);
    Optional<Review> findByReviewerIdAndTargetTypeAndTargetId(UUID reviewerId, ReviewTargetType targetType, UUID targetId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.isHidden = false AND r.isPublic = true")
    Long countPublicByTarget(@Param("targetType") ReviewTargetType targetType, @Param("targetId") UUID targetId);

    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.isHidden = false AND r.isPublic = true")
    BigDecimal averagePublicRatingByTarget(@Param("targetType") ReviewTargetType targetType, @Param("targetId") UUID targetId);
}

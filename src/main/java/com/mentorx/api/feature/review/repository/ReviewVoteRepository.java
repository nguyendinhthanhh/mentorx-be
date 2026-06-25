package com.mentorx.api.feature.review.repository;

import com.mentorx.api.feature.review.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, UUID> {
    Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);
    List<ReviewVote> findByUserIdAndReviewIdIn(UUID userId, List<UUID> reviewIds);
}

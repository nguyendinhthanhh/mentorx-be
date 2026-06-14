package com.mentorx.api.feature.review.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.review.dto.request.ReviewCreateRequest;
import com.mentorx.api.feature.review.dto.request.ReviewResponseRequest;
import com.mentorx.api.feature.review.dto.request.ReviewUpdateRequest;
import com.mentorx.api.feature.review.dto.response.ReviewResponse;
import com.mentorx.api.feature.review.entity.Review;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.review.repository.ReviewRepository;
import com.mentorx.api.feature.review.service.ReviewService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID currentUserId, ReviewCreateRequest request) {
        if (reviewRepository.findByReviewerIdAndTargetTypeAndTargetId(
                currentUserId, request.targetType(), request.targetId()).isPresent()) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateReviewEligibility(reviewer, request);

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setTargetType(request.targetType());
        review.setTargetId(request.targetId());
        review.setOverallRating(request.overallRating());
        review.setCommunicationRating(request.communicationRating());
        review.setQualityRating(request.qualityRating());
        review.setTimelinessRating(request.timelinessRating());
        review.setProfessionalismRating(request.professionalismRating());
        review.setValueRating(request.valueRating());
        review.setReviewText(request.reviewText());
        review.setReviewTitle(request.reviewTitle());
        review.setPros(request.pros());
        review.setCons(request.cons());
        review.setIsAnonymous(request.isAnonymous() != null ? request.isAnonymous() : false);
        review.setIsPublic(request.isPublic() != null ? request.isPublic() : true);
        review.setLanguage(request.language());
        review.setContractId(request.contractId());

        Review saved = reviewRepository.save(review);
        syncCourseRatingIfNeeded(saved.getTargetType(), saved.getTargetId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID currentUserId, UUID reviewId, ReviewUpdateRequest request) {
        Review review = findReview(reviewId);
        if (!review.getReviewer().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        
        if (!review.canBeEdited()) {
            throw new AppException(ErrorCode.REVIEW_CANNOT_BE_EDITED);
        }

        if (request.overallRating() != null) review.setOverallRating(request.overallRating());
        if (request.communicationRating() != null) review.setCommunicationRating(request.communicationRating());
        if (request.qualityRating() != null) review.setQualityRating(request.qualityRating());
        if (request.timelinessRating() != null) review.setTimelinessRating(request.timelinessRating());
        if (request.professionalismRating() != null) review.setProfessionalismRating(request.professionalismRating());
        if (request.valueRating() != null) review.setValueRating(request.valueRating());
        if (request.reviewText() != null) review.setReviewText(request.reviewText());
        if (request.reviewTitle() != null) review.setReviewTitle(request.reviewTitle());
        if (request.pros() != null) review.setPros(request.pros());
        if (request.cons() != null) review.setCons(request.cons());
        if (request.isAnonymous() != null) review.setIsAnonymous(request.isAnonymous());
        if (request.isPublic() != null) review.setIsPublic(request.isPublic());
        if (request.language() != null) review.setLanguage(request.language());

        Review saved = reviewRepository.save(review);
        syncCourseRatingIfNeeded(saved.getTargetType(), saved.getTargetId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse respondToReview(UUID currentUserId, UUID reviewId, ReviewResponseRequest request) {
        Review review = findReview(reviewId);
        if (review.getTargetType() != ReviewTargetType.COURSE) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
        Course course = courseRepository.findById(review.getTargetId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (!course.getInstructor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        review.setResponseText(request.responseText().trim());
        review.setResponseAt(LocalDateTime.now());
        review.setResponseByUserId(currentUserId);
        return toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse getReviewById(UUID reviewId) {
        return toResponse(findReview(reviewId));
    }

    @Override
    public Page<ReviewResponse> getReviewsByTarget(ReviewTargetType targetType, UUID targetId, Pageable pageable) {
        return reviewRepository.findByTargetTypeAndTargetIdAndIsHiddenFalseAndIsPublicTrue(targetType, targetId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ReviewResponse> getReviewsByReviewer(UUID reviewerId, Pageable pageable) {
        return reviewRepository.findByReviewerId(reviewerId, pageable).map(this::toResponse);
    }

    @Override
    public boolean canReviewMentor(UUID currentUserId, UUID mentorId) {
        if (currentUserId.equals(mentorId)) {
            return false;
        }

        return contractRepository.existsClientMentorRelationshipExcludingStatus(
                currentUserId,
                mentorId,
                ContractStatus.DRAFT
        );
    }

    @Override
    @Transactional
    public ReviewResponse voteHelpful(UUID reviewId, boolean isHelpful) {
        Review review = findReview(reviewId);
        if (isHelpful) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        } else {
            review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
        }
        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(UUID currentUserId, UUID reviewId) {
        Review review = findReview(reviewId);
        if (!review.getReviewer().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        review.setIsHidden(true);
        review.setHiddenReason("Deleted by user/admin");
        Review saved = reviewRepository.save(review);
        syncCourseRatingIfNeeded(saved.getTargetType(), saved.getTargetId());
    }

    private Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateReviewEligibility(User reviewer, ReviewCreateRequest request) {
        if (request.targetType() != ReviewTargetType.MENTOR) {
            if (request.targetType() == ReviewTargetType.COURSE) {
                validateCourseReviewEligibility(reviewer, request.targetId());
            }
            return;
        }

        if (reviewer.getId().equals(request.targetId())) {
            throw new AppException(ErrorCode.CANNOT_REVIEW_SELF);
        }

        if (!canReviewMentor(reviewer.getId(), request.targetId())) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
    }

    private void validateCourseReviewEligibility(User reviewer, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (course.getInstructor().getId().equals(reviewer.getId())) {
            throw new AppException(ErrorCode.CANNOT_REVIEW_SELF);
        }
        if (!courseEnrollmentRepository.existsByCourseIdAndStudentId(courseId, reviewer.getId())) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
    }

    private void syncCourseRatingIfNeeded(ReviewTargetType targetType, UUID targetId) {
        if (targetType != ReviewTargetType.COURSE) return;
        courseRepository.findById(targetId).ifPresent(course -> {
            Long total = reviewRepository.countPublicByTarget(ReviewTargetType.COURSE, targetId);
            BigDecimal average = reviewRepository.averagePublicRatingByTarget(ReviewTargetType.COURSE, targetId);
            course.setTotalReviews(total == null ? 0 : total.intValue());
            course.setAverageRating(average == null ? BigDecimal.ZERO : average.setScale(2, RoundingMode.HALF_UP));
            courseRepository.save(course);
        });
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getReviewer().getId(),
                review.getReviewerDisplayName(),
                review.getTargetType(),
                review.getTargetId(),
                review.getOverallRating(),
                review.getCommunicationRating(),
                review.getQualityRating(),
                review.getTimelinessRating(),
                review.getProfessionalismRating(),
                review.getValueRating(),
                review.getReviewText(),
                review.getReviewTitle(),
                review.getPros(),
                review.getCons(),
                review.getIsVerified(),
                review.getVerifiedAt(),
                review.getIsAnonymous(),
                review.getIsPublic(),
                review.getIsFeatured(),
                review.getHelpfulCount(),
                review.getNotHelpfulCount(),
                review.getReportCount(),
                review.getIsModerated(),
                review.getModeratedAt(),
                review.getModerationNotes(),
                review.getIsHidden(),
                review.getHiddenReason(),
                review.getLanguage(),
                review.getContractId(),
                review.getWouldRecommend(),
                review.getResponseText(),
                review.getResponseAt(),
                review.getResponseByUserId(),
                review.getHelpfulnessRatio(),
                review.canBeEdited(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

package com.mentorx.api.feature.review.controller;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.review.dto.request.ReviewCreateRequest;
import com.mentorx.api.feature.review.dto.request.ReviewUpdateRequest;
import com.mentorx.api.feature.review.dto.response.ReviewResponse;
import com.mentorx.api.feature.review.enums.ReviewTargetType;
import com.mentorx.api.feature.review.service.ReviewService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication) {
        UUID currentUserId = resolveCurrentUser(authentication).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(reviewService.createReview(currentUserId, request)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.updateReview(reviewId, request)));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewById(reviewId)));
    }

    @GetMapping("/target/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getByTarget(
            @PathVariable ReviewTargetType targetType,
            @PathVariable UUID targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByTarget(targetType, targetId, PageRequest.of(page, size))));
    }

    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getByReviewer(
            @PathVariable UUID reviewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByReviewer(reviewerId, PageRequest.of(page, size))));
    }

    @GetMapping("/eligibility/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<Boolean>> canReviewMentor(
            @PathVariable UUID mentorId,
            Authentication authentication) {
        UUID currentUserId = resolveCurrentUser(authentication).getId();
        return ResponseEntity.ok(ApiResponse.success(reviewService.canReviewMentor(currentUserId, mentorId)));
    }

    @PostMapping("/{reviewId}/vote")
    public ResponseEntity<ApiResponse<ReviewResponse>> vote(
            @PathVariable UUID reviewId,
            @RequestParam boolean isHelpful) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.voteHelpful(reviewId, isHelpful)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}

package com.mentorx.api.feature.review.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ReviewUpdateRequest(
        @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
        @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
        BigDecimal overallRating,
        
        @DecimalMin(value = "1.0") @DecimalMax(value = "5.0") BigDecimal communicationRating,
        @DecimalMin(value = "1.0") @DecimalMax(value = "5.0") BigDecimal qualityRating,
        @DecimalMin(value = "1.0") @DecimalMax(value = "5.0") BigDecimal timelinessRating,
        @DecimalMin(value = "1.0") @DecimalMax(value = "5.0") BigDecimal professionalismRating,
        @DecimalMin(value = "1.0") @DecimalMax(value = "5.0") BigDecimal valueRating,
        
        @Size(max = 2000, message = "Review text must not exceed 2000 characters") String reviewText,
        @Size(max = 200, message = "Review title must not exceed 200 characters") String reviewTitle,
        @Size(max = 1000, message = "Pros must not exceed 1000 characters") String pros,
        @Size(max = 1000, message = "Cons must not exceed 1000 characters") String cons,
        
        Boolean isAnonymous,
        Boolean isPublic,
        
        @Size(max = 10) String language
) {}

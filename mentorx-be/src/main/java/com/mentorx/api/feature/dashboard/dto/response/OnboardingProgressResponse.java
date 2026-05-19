package com.mentorx.api.feature.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for onboarding progress
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingProgressResponse {
    
    /**
     * Whether onboarding is complete
     */
    private Boolean isComplete;
    
    /**
     * Completion percentage (0-100)
     */
    private Integer completionPercentage;
    
    /**
     * Current step in onboarding flow
     */
    private String currentStep;
    
    /**
     * Total number of steps
     */
    private Integer totalSteps;
}

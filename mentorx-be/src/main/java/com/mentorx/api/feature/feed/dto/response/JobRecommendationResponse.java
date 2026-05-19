package com.mentorx.api.feature.feed.dto.response;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for job recommendations with match score
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationResponse {
    
    private UUID jobId;
    private String title;
    private String description;
    private JobType jobType;
    private BudgetType budgetType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal hourlyRate;
    private BigDecimal estimatedHours;
    private LocalDateTime deadlineAt;
    private String clientName;
    private UUID clientId;
    private Integer categoryId;
    private String categoryName;
    private List<String> requiredSkills;
    private Integer proposalCount;
    private LocalDateTime publishedAt;
    private BigDecimal matchScore;
    private Boolean isFeatured;
}

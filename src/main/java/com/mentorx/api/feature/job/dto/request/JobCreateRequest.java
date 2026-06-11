package com.mentorx.api.feature.job.dto.request;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.common.enums.JobVisibility;
import com.mentorx.api.common.enums.UserLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JobCreateRequest(
        @NotNull UUID clientId,
        Integer categoryId,
        @Size(max = 120) String customCategoryName,
        JobType jobType,
        
        @Size(max = 200) 
        String title,
        
        @Size(max = 5000) 
        String description,
        
        List<String> requiredSkills,
        String experienceLevel,
        UserLevel currentLevel,
        
        @Size(max = 5000) String learningGoals,
        @Size(max = 5000) String successCriteria,
        @Size(max = 5000) String availabilityExpectation,
        @Size(max = 120) String availabilityStartTime,
        @Size(max = 120) String availabilityEndTime,
        @Size(max = 120) String communicationPreference,
        
        BudgetType budgetType,
        BigDecimal budgetMinMxc,
        BigDecimal budgetMaxMxc,
        BigDecimal hourlyRateMxc,
        BigDecimal estimatedHours,
        @Future LocalDateTime deadlineAt,
        
        String timezone,
        Integer expectedSessions,
        Integer expectedWeeks,
        JobVisibility visibility,
        String preferredLanguage,
        
        JobStatus status,
        String attachmentUrl,
        List<String> attachments
) {}

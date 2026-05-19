package com.mentorx.api.feature.job.dto.response;

import com.mentorx.api.feature.job.enums.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProposalResponse(
        UUID id,
        UUID jobId,
        String jobTitle,
        UUID mentorId,
        String mentorName,
        ProposalStatus status,
        String coverLetter,
        BigDecimal proposedAmount,
        BigDecimal proposedHourlyRate,
        Integer estimatedDurationDays,
        LocalDate proposedStartDate,
        LocalDate proposedDeliveryDate,
        List<Map<String, Object>> proposedMilestones,
        String relevantExperience,
        List<String> portfolioLinks,
        List<String> attachments,
        String questions,
        String terms,
        LocalDateTime submittedAt,
        Boolean isFeatured,
        BigDecimal score,
        Boolean isCounterProposal,
        Integer viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

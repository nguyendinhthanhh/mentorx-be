package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobSort;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.job.dto.request.JobCreateRequest;
import com.mentorx.api.feature.job.dto.request.JobUpdateRequest;
import com.mentorx.api.feature.job.dto.response.JobResponse;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.service.JobService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public JobResponse create(JobCreateRequest request) {
        requireCurrentUser(request.clientId());
        JobStatus status = request.status() != null ? request.status() : JobStatus.OPEN;
        if (status == JobStatus.OPEN) {
            validateOpenJobRequirements(
                    request.title(),
                    request.description(),
                    request.budgetType(),
                    request.jobType(),
                    request.categoryId(),
                    request.customCategoryName(),
                    request.requiredSkills()
            );
            validateBudget(request.budgetType(), request.budgetMinMxc(), request.budgetMaxMxc(), request.hourlyRateMxc());
        }

        User client = userRepository.findById(request.clientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Job job = Job.builder()
                .client(client)
                .categoryId(request.categoryId())
                .customCategoryName(normalizeText(request.customCategoryName()))
                .jobType(request.jobType())
                .title(request.title())
                .description(request.description())
                .requiredSkills(normalizeList(request.requiredSkills()))
                .experienceLevel(request.experienceLevel())
                .currentLevel(request.currentLevel())
                .learningGoals(request.learningGoals())
                .successCriteria(request.successCriteria())
                .availabilityExpectation(normalizeText(request.availabilityExpectation()))
                .availabilityStartTime(normalizeText(request.availabilityStartTime()))
                .availabilityEndTime(normalizeText(request.availabilityEndTime()))
                .communicationPreference(normalizeText(request.communicationPreference()))
                .timezone(request.timezone())
                .expectedSessions(request.expectedSessions())
                .expectedWeeks(request.expectedWeeks())
                .visibility(request.visibility() != null ? request.visibility() : com.mentorx.api.common.enums.JobVisibility.PUBLIC)
                .preferredLanguage(request.preferredLanguage())
                .budgetType(request.budgetType())
                .budgetMinMxc(request.budgetMinMxc())
                .budgetMaxMxc(request.budgetMaxMxc())
                .hourlyRateMxc(request.hourlyRateMxc())
                .estimatedHours(request.estimatedHours())
                .deadlineAt(request.deadlineAt())
                .attachmentUrl(resolvePrimaryAttachment(request.attachmentUrl(), request.attachments()))
                .attachments(normalizeList(request.attachments()))
                .status(request.status() != null ? request.status() : JobStatus.OPEN)
                .publishedAt(request.status() == JobStatus.OPEN || request.status() == null ? LocalDateTime.now() : null)
                .build();
        return toResponse(jobRepository.save(job));
    }

    @Override
    public JobResponse getById(UUID jobId) {
        return toResponse(findJob(jobId));
    }

    @Override
    @Transactional
    public JobResponse update(UUID jobId, JobUpdateRequest request) {
        Job job = findJob(jobId);
        requireJobOwnerOrAdmin(job);
        ensureJobEditable(job);
        if (request.categoryId() != null) job.setCategoryId(request.categoryId());
        if (request.customCategoryName() != null) job.setCustomCategoryName(normalizeText(request.customCategoryName()));
        if (request.jobType() != null) job.setJobType(request.jobType());
        if (request.title() != null) job.setTitle(request.title());
        if (request.description() != null) job.setDescription(request.description());
        if (request.requiredSkills() != null) job.setRequiredSkills(normalizeList(request.requiredSkills()));
        if (request.experienceLevel() != null) job.setExperienceLevel(request.experienceLevel());
        if (request.currentLevel() != null) job.setCurrentLevel(request.currentLevel());
        if (request.learningGoals() != null) job.setLearningGoals(request.learningGoals());
        if (request.successCriteria() != null) job.setSuccessCriteria(request.successCriteria());
        if (request.availabilityExpectation() != null) job.setAvailabilityExpectation(normalizeText(request.availabilityExpectation()));
        if (request.availabilityStartTime() != null) job.setAvailabilityStartTime(normalizeText(request.availabilityStartTime()));
        if (request.availabilityEndTime() != null) job.setAvailabilityEndTime(normalizeText(request.availabilityEndTime()));
        if (request.communicationPreference() != null) job.setCommunicationPreference(normalizeText(request.communicationPreference()));
        if (request.timezone() != null) job.setTimezone(request.timezone());
        if (request.expectedSessions() != null) job.setExpectedSessions(request.expectedSessions());
        if (request.expectedWeeks() != null) job.setExpectedWeeks(request.expectedWeeks());
        if (request.visibility() != null) job.setVisibility(request.visibility());
        if (request.preferredLanguage() != null) job.setPreferredLanguage(request.preferredLanguage());
        if (request.budgetType() != null) {
            job.setBudgetType(request.budgetType());
        }
        
        JobStatus newStatus = request.status() != null ? request.status() : job.getStatus();
        if (newStatus == JobStatus.OPEN) {
            validateOpenJobRequirements(
                request.title() != null ? request.title() : job.getTitle(),
                request.description() != null ? request.description() : job.getDescription(),
                request.budgetType() != null ? request.budgetType() : job.getBudgetType(),
                request.jobType() != null ? request.jobType() : job.getJobType(),
                request.categoryId() != null ? request.categoryId() : job.getCategoryId(),
                request.customCategoryName() != null ? request.customCategoryName() : job.getCustomCategoryName(),
                request.requiredSkills() != null ? request.requiredSkills() : job.getRequiredSkills()
            );
            validateBudget(
                job.getBudgetType(), 
                request.budgetMinMxc() != null ? request.budgetMinMxc() : job.getBudgetMinMxc(), 
                request.budgetMaxMxc() != null ? request.budgetMaxMxc() : job.getBudgetMaxMxc(), 
                request.hourlyRateMxc() != null ? request.hourlyRateMxc() : job.getHourlyRateMxc()
            );
        }
        
        if (request.budgetMinMxc() != null) job.setBudgetMinMxc(request.budgetMinMxc());
        if (request.budgetMaxMxc() != null) job.setBudgetMaxMxc(request.budgetMaxMxc());
        if (request.hourlyRateMxc() != null) job.setHourlyRateMxc(request.hourlyRateMxc());
        if (request.estimatedHours() != null) job.setEstimatedHours(request.estimatedHours());
        if (request.deadlineAt() != null) job.setDeadlineAt(request.deadlineAt());
        if (request.isFeatured() != null) job.setIsFeatured(request.isFeatured());
        if (request.attachments() != null) {
            List<String> normalizedAttachments = normalizeList(request.attachments());
            job.setAttachments(normalizedAttachments);
            job.setAttachmentUrl(resolvePrimaryAttachment(request.attachmentUrl(), normalizedAttachments));
        } else if (request.attachmentUrl() != null) {
            job.setAttachmentUrl(request.attachmentUrl().trim());
        }
        if (request.status() != null) {
            job.setStatus(request.status());
            if (request.status() == JobStatus.OPEN && job.getPublishedAt() == null) {
                job.setPublishedAt(LocalDateTime.now());
            }
            if (request.status() == JobStatus.CLOSED || request.status() == JobStatus.CANCELLED) {
                job.setClosedAt(LocalDateTime.now());
            }
        }
        return toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void delete(UUID jobId) {
        Job job = findJob(jobId);
        requireJobOwnerOrAdmin(job);
        ensureNoActiveContract(job.getId(), "Cannot delete a job while it has an active contract.");
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Override
    public Page<JobResponse> getOpenJobs(JobType jobType, Integer categoryId, String skillKeyword,
                                         String experienceLevel, String keyword, BigDecimal budgetMin, BigDecimal budgetMax,
                                         String budgetType, JobStatus status, JobSort sort, Pageable pageable) {
        String jobTypeStr = jobType != null ? jobType.name() : null;
        String skillNorm = normalizeText(skillKeyword);
        String keywordNorm = normalizeText(keyword);
        String budgetTypeStr = budgetType != null ? budgetType : null;
        String statusStr = status != null ? status.name() : null;

        if (sort == null) {
            sort = JobSort.NEWEST;
        }
        if (sort == JobSort.RELEVANCE && keywordNorm == null) {
            sort = JobSort.NEWEST;
        }

        Page<Object[]> result = switch (sort) {
            case RELEVANCE -> jobRepository.findOpenRelevance(statusStr, jobTypeStr, categoryId, skillNorm, experienceLevel, keywordNorm, budgetMin, budgetMax, budgetTypeStr, pageable);
            case BUDGET_DESC -> jobRepository.findOpenBudgetDesc(statusStr, jobTypeStr, categoryId, skillNorm, experienceLevel, keywordNorm, budgetMin, budgetMax, budgetTypeStr, pageable);
            case BUDGET_ASC -> jobRepository.findOpenBudgetAsc(statusStr, jobTypeStr, categoryId, skillNorm, experienceLevel, keywordNorm, budgetMin, budgetMax, budgetTypeStr, pageable);
            case POPULAR -> jobRepository.findOpenPopular(statusStr, jobTypeStr, categoryId, skillNorm, experienceLevel, keywordNorm, budgetMin, budgetMax, budgetTypeStr, pageable);
            default -> jobRepository.findOpenWithAllFilters(statusStr, jobTypeStr, categoryId, skillNorm, experienceLevel, keywordNorm, budgetMin, budgetMax, budgetTypeStr, pageable);
        };

        return result.map(this::toResponseWithScore);
    }

    private JobResponse toResponseWithScore(Object[] row) {
        UUID jobId = (UUID) row[0];
        String title = (String) row[34];
        String description = (String) row[17];
        String jobTypeStr = (String) row[24];
        JobType jobType = JobType.valueOf(jobTypeStr);
        String statusStr = (String) row[30];
        JobStatus status = JobStatus.valueOf(statusStr);
        BigDecimal budgetMax = (BigDecimal) row[7];
        BigDecimal budgetMin = (BigDecimal) row[8];
        String budgetTypeStr = (String) row[9];
        BudgetType budgetType = BudgetType.valueOf(budgetTypeStr);
        BigDecimal hourlyRate = (BigDecimal) row[22];
        Integer viewCount = (Integer) row[35];
        Integer proposalCount = (Integer) row[27];
        Boolean isFeatured = (Boolean) row[23];
        java.sql.Timestamp createdAt = (java.sql.Timestamp) row[1];
        java.sql.Timestamp updatedAt = (java.sql.Timestamp) row[2];
        java.sql.Timestamp publishedAt = (java.sql.Timestamp) row[28];
        java.sql.Timestamp closedAt = (java.sql.Timestamp) row[11];
        java.sql.Timestamp deadlineAt = (java.sql.Timestamp) row[15];
        java.sql.Timestamp startDate = (java.sql.Timestamp) row[29];
        String statusReason = (String) row[31];
        String attachmentUrl = (String) row[3];
        Integer categoryId = (Integer) row[10];
        String customCategoryName = (String) row[14];
        java.math.BigDecimal estimatedHoursBig = (java.math.BigDecimal) row[18];
        BigDecimal estimatedHours = estimatedHoursBig;
        String experienceLevel = (String) row[21];
        String currentLevel = (String) row[13];
        String learningGoals = (String) row[25];
        String successCriteria = (String) row[32];
        String availabilityExpectation = (String) row[5];
        String availabilityStartTime = (String) row[6];
        String availabilityEndTime = (String) row[4];
        String communicationPreference = (String) row[12];
        UUID clientId = (UUID) row[37];
        String clientName = (String) row[38];
        Double score = null;
        if (row.length > 39 && row[39] instanceof Number) {
            score = ((Number) row[39]).doubleValue();
        }
        return new JobResponse(
                jobId,
                clientId,
                clientName,
                categoryId,
                customCategoryName,
                jobType,
                title,
                description,
                new ArrayList<>(),
                experienceLevel,
                currentLevel,
                learningGoals,
                successCriteria,
                availabilityExpectation,
                availabilityStartTime,
                availabilityEndTime,
                communicationPreference,
                budgetType,
                budgetMin,
                budgetMax,
                hourlyRate,
                estimatedHours,
                startDate != null ? startDate.toLocalDateTime() : null,
                deadlineAt != null ? deadlineAt.toLocalDateTime() : null,
                status,
                isFeatured != null && isFeatured,
                viewCount,
                proposalCount,
                publishedAt != null ? publishedAt.toLocalDateTime() : null,
                closedAt != null ? closedAt.toLocalDateTime() : null,
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null,
                statusReason,
                attachmentUrl,
                new ArrayList<>(),
                score
        );
    }

    @Override
    public Page<JobResponse> getAllJobs(JobStatus status, JobType jobType, Integer categoryId, String skillKeyword, Pageable pageable) {
        return jobRepository.findAllWithAdvancedFilters(
                status != null ? status.name() : null,
                jobType != null ? jobType.name() : null,
                categoryId,
                normalizeText(skillKeyword),
                pageable
        ).map(this::toResponse);
    }

    @Override
    public Page<JobResponse> getByClient(UUID clientId, Pageable pageable) {
        mentorModeAccessService.requireSelfOrAdmin(clientId);
        return jobRepository.findByClientId(clientId, pageable).map(this::toResponse);
    }

    @Override
    public Page<JobResponse> getByStatus(JobStatus status, Pageable pageable) {
        return jobRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public JobResponse updateStatus(UUID jobId, JobStatus status, String reason) {
        Job job = findJob(jobId);
        requireJobOwnerOrAdmin(job);
        validateManualStatusTransition(job, status);
        job.setStatus(status);
        if (reason != null) {
            job.setStatusReason(reason);
        }
        if (status == JobStatus.OPEN && job.getPublishedAt() == null) {
            job.setPublishedAt(LocalDateTime.now());
        }
        if (status == JobStatus.OPEN) {
            job.setClosedAt(null);
        }
        if (status == JobStatus.CLOSED || status == JobStatus.CANCELLED) {
            job.setClosedAt(LocalDateTime.now());
        }
        job.setUpdatedAt(LocalDateTime.now());
        return toResponse(jobRepository.save(job));
    }

    private Job findJob(UUID jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
    }

    private void requireCurrentUser(UUID userId) {
        if (!mentorModeAccessService.getCurrentUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot create a job for another user.");
        }
    }

    private void requireJobOwnerOrAdmin(Job job) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        if (!mentorModeAccessService.isCurrentUserAdmin() && !job.getClient().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot manage another user's job.");
        }
    }

    private void ensureJobEditable(Job job) {
        if (job.getStatus() == JobStatus.IN_PROGRESS
                || job.getStatus() == JobStatus.COMPLETED
                || job.getStatus() == JobStatus.CANCELLED
                || hasActiveContract(job.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This job can no longer be edited because an active deal already exists.");
        }
    }

    private void ensureNoActiveContract(UUID jobId, String message) {
        if (hasActiveContract(jobId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private boolean hasActiveContract(UUID jobId) {
        return contractRepository.existsByJobIdAndStatusIn(jobId, List.of(
                ContractStatus.ACTIVE,
                ContractStatus.PENDING_PAYMENT,
                ContractStatus.PAUSED,
                ContractStatus.IN_DISPUTE,
                ContractStatus.UNDER_REVIEW
        ));
    }

    private void validateManualStatusTransition(Job job, JobStatus nextStatus) {
        if (nextStatus == null || nextStatus == job.getStatus()) {
            return;
        }

        if (nextStatus == JobStatus.CLOSED) {
            if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.DRAFT) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Only draft or open jobs can be closed.");
            }
            ensureNoActiveContract(job.getId(), "You cannot close a job that already has an active contract.");
            return;
        }

        if (nextStatus == JobStatus.OPEN) {
            ensureNoActiveContract(job.getId(), "You cannot reopen a job while an active contract exists.");
            validateOpenJobRequirements(
                    job.getTitle(),
                    job.getDescription(),
                    job.getBudgetType(),
                    job.getJobType(),
                    job.getCategoryId(),
                    job.getCustomCategoryName(),
                    job.getRequiredSkills()
            );
            validateBudget(job.getBudgetType(), job.getBudgetMinMxc(), job.getBudgetMaxMxc(), job.getHourlyRateMxc());
            return;
        }

        if (nextStatus == JobStatus.CANCELLED) {
            ensureNoActiveContract(job.getId(), "You cannot cancel a job while an active contract exists.");
            return;
        }

        if (nextStatus == JobStatus.IN_PROGRESS || nextStatus == JobStatus.COMPLETED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "This status transition must be driven by contract actions.");
        }
    }

    private JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getClient().getId(),
                job.getClient().getFullName(),
                job.getCategoryId(),
                job.getCustomCategoryName(),
                job.getJobType(),
                job.getTitle(),
                job.getDescription(),
                job.getRequiredSkills() != null ? new ArrayList<>(job.getRequiredSkills()) : new ArrayList<>(),
                job.getExperienceLevel(),
                job.getCurrentLevel() != null ? job.getCurrentLevel().name() : null,
                job.getLearningGoals(),
                job.getSuccessCriteria(),
                job.getAvailabilityExpectation(),
                job.getAvailabilityStartTime(),
                job.getAvailabilityEndTime(),
                job.getCommunicationPreference(),
                job.getBudgetType(),
                job.getBudgetMinMxc(),
                job.getBudgetMaxMxc(),
                job.getHourlyRateMxc(),
                job.getEstimatedHours(),
                job.getStartDate(),
                job.getDeadlineAt(),
                job.getStatus(),
                job.getIsFeatured(),
                job.getViewCount(),
                job.getProposalCount(),
                job.getPublishedAt(),
                job.getClosedAt(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                job.getStatusReason(),
                job.getAttachmentUrl(),
                job.getAttachments() != null ? new ArrayList<>(job.getAttachments()) : new ArrayList<>(),
                null
        );
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolvePrimaryAttachment(String attachmentUrl, List<String> attachments) {
        String normalizedPrimary = normalizeText(attachmentUrl);
        if (normalizedPrimary != null) {
            return normalizedPrimary;
        }
        List<String> normalizedAttachments = normalizeList(attachments);
        return normalizedAttachments.isEmpty() ? null : normalizedAttachments.get(0);
    }

    private void validateBudget(BudgetType type, java.math.BigDecimal min, java.math.BigDecimal max, java.math.BigDecimal hourly) {
        if (type == BudgetType.FIXED) {
            if (min == null && max == null) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
        } else if (type == BudgetType.HOURLY) {
            if (hourly == null) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
        }
    }

    private void validateOpenJobRequirements(
            String title,
            String description,
            BudgetType budgetType,
            JobType jobType,
            Integer categoryId,
            String customCategoryName,
            List<String> requiredSkills
    ) {
        if (title == null || title.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        if (description == null || description.isBlank() || description.trim().split("\\s+").length < 10) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        if (budgetType == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        if (jobType == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        if (categoryId == null && (customCategoryName == null || customCategoryName.isBlank())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Job domain/category is required");
        }
        if (requiredSkills == null || normalizeList(requiredSkills).isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "At least one required skill is required");
        }
    }
}

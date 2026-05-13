package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.common.enums.BudgetType;
import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.job.dto.request.JobCreateRequest;
import com.mentorx.api.feature.job.dto.request.JobUpdateRequest;
import com.mentorx.api.feature.job.dto.response.JobResponse;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.service.JobService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public JobResponse create(JobCreateRequest request) {
        JobStatus status = request.status() != null ? request.status() : JobStatus.OPEN;
        if (status == JobStatus.OPEN) {
            validateOpenJobRequirements(request.title(), request.description(), request.budgetType(), request.jobType());
            validateBudget(request.budgetType(), request.budgetMinMxc(), request.budgetMaxMxc(), request.hourlyRateMxc());
        }

        User client = userRepository.findById(request.clientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Job job = Job.builder()
                .client(client)
                .categoryId(request.categoryId())
                .jobType(request.jobType())
                .title(request.title())
                .description(request.description())
                .requiredSkills(normalizeList(request.requiredSkills()))
                .experienceLevel(request.experienceLevel())
                .currentLevel(request.currentLevel())
                .learningGoals(request.learningGoals())
                .successCriteria(request.successCriteria())
                .availabilityExpectation(request.availabilityExpectation())
                .communicationPreference(request.communicationPreference())
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
                .attachmentUrl(request.attachmentUrl())
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
        if (request.categoryId() != null) job.setCategoryId(request.categoryId());
        if (request.jobType() != null) job.setJobType(request.jobType());
        if (request.title() != null) job.setTitle(request.title());
        if (request.description() != null) job.setDescription(request.description());
        if (request.requiredSkills() != null) job.setRequiredSkills(normalizeList(request.requiredSkills()));
        if (request.experienceLevel() != null) job.setExperienceLevel(request.experienceLevel());
        if (request.currentLevel() != null) job.setCurrentLevel(request.currentLevel());
        if (request.learningGoals() != null) job.setLearningGoals(request.learningGoals());
        if (request.successCriteria() != null) job.setSuccessCriteria(request.successCriteria());
        if (request.availabilityExpectation() != null) job.setAvailabilityExpectation(request.availabilityExpectation());
        if (request.communicationPreference() != null) job.setCommunicationPreference(request.communicationPreference());
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
                request.jobType() != null ? request.jobType() : job.getJobType()
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
        if (request.attachmentUrl() != null) job.setAttachmentUrl(request.attachmentUrl());
        if (request.attachments() != null) job.setAttachments(normalizeList(request.attachments()));
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
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Override
    public Page<JobResponse> getOpenJobs(JobType jobType, Integer categoryId, Pageable pageable) {
        return jobRepository.findOpenWithFilters(jobType, categoryId, pageable).map(this::toResponse);
    }

    @Override
    public Page<JobResponse> getAllJobs(JobStatus status, JobType jobType, Integer categoryId, Pageable pageable) {
        return jobRepository.findAllWithFilters(status, jobType, categoryId, pageable).map(this::toResponse);
    }

    @Override
    public Page<JobResponse> getByClient(UUID clientId, Pageable pageable) {
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
        job.setStatus(status);
        if (reason != null) {
            job.setStatusReason(reason);
        }
        if (status == JobStatus.OPEN && job.getPublishedAt() == null) {
            job.setPublishedAt(LocalDateTime.now());
        }
        if (status == JobStatus.CLOSED || status == JobStatus.CANCELLED) {
            job.setClosedAt(LocalDateTime.now());
        }
        return toResponse(jobRepository.save(job));
    }

    private Job findJob(UUID jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
    }

    private JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getClient().getId(),
                job.getClient().getFullName(),
                job.getCategoryId(),
                job.getJobType(),
                job.getTitle(),
                job.getDescription(),
                job.getRequiredSkills() != null ? new ArrayList<>(job.getRequiredSkills()) : new ArrayList<>(),
                job.getExperienceLevel(),
                job.getCurrentLevel() != null ? job.getCurrentLevel().name() : null,
                job.getLearningGoals(),
                job.getSuccessCriteria(),
                job.getAvailabilityExpectation(),
                job.getCommunicationPreference() != null ? job.getCommunicationPreference().name() : null,
                job.getBudgetType(),
                job.getBudgetMinMxc(),
                job.getBudgetMaxMxc(),
                job.getHourlyRateMxc(),
                job.getEstimatedHours(),
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
                job.getAttachments() != null ? new ArrayList<>(job.getAttachments()) : new ArrayList<>()
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

    private void validateOpenJobRequirements(String title, String description, BudgetType budgetType, JobType jobType) {
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
    }
}

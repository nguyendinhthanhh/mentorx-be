package com.mentorx.api.feature.job.service.impl;

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
        User client = userRepository.findById(request.clientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Job job = Job.builder()
                .client(client)
                .categoryId(request.categoryId())
                .jobType(request.jobType())
                .title(request.title())
                .description(request.description())
                .budgetType(request.budgetType())
                .budgetMinMxc(request.budgetMinMxc())
                .budgetMaxMxc(request.budgetMaxMxc())
                .hourlyRateMxc(request.hourlyRateMxc())
                .estimatedHours(request.estimatedHours())
                .deadlineAt(request.deadlineAt())
                .attachmentUrl(request.attachmentUrl())
                .attachments(request.attachments())
                .status(JobStatus.DRAFT)
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
        if (request.budgetType() != null) job.setBudgetType(request.budgetType());
        if (request.budgetMinMxc() != null) job.setBudgetMinMxc(request.budgetMinMxc());
        if (request.budgetMaxMxc() != null) job.setBudgetMaxMxc(request.budgetMaxMxc());
        if (request.hourlyRateMxc() != null) job.setHourlyRateMxc(request.hourlyRateMxc());
        if (request.estimatedHours() != null) job.setEstimatedHours(request.estimatedHours());
        if (request.deadlineAt() != null) job.setDeadlineAt(request.deadlineAt());
        if (request.isFeatured() != null) job.setIsFeatured(request.isFeatured());
        if (request.attachmentUrl() != null) job.setAttachmentUrl(request.attachmentUrl());
        if (request.attachments() != null) job.setAttachments(request.attachments());
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
}

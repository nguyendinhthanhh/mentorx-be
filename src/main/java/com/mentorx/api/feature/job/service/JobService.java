package com.mentorx.api.feature.job.service;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.job.dto.request.JobCreateRequest;
import com.mentorx.api.feature.job.dto.request.JobUpdateRequest;
import com.mentorx.api.feature.job.dto.response.JobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobService {
    JobResponse create(JobCreateRequest request);
    JobResponse getById(UUID jobId);
    JobResponse update(UUID jobId, JobUpdateRequest request);
    void delete(UUID jobId);
    Page<JobResponse> getOpenJobs(JobType jobType, Integer categoryId, String skillKeyword, Pageable pageable);
    Page<JobResponse> getAllJobs(JobStatus status, JobType jobType, Integer categoryId, String skillKeyword, Pageable pageable);
    Page<JobResponse> getByClient(UUID clientId, Pageable pageable);
    Page<JobResponse> getByStatus(JobStatus status, Pageable pageable);
    JobResponse updateStatus(UUID jobId, JobStatus status, String reason);
}

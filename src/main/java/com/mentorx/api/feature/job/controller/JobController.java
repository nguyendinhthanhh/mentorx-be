package com.mentorx.api.feature.job.controller;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.job.dto.request.JobCreateRequest;
import com.mentorx.api.feature.job.dto.request.JobUpdateRequest;
import com.mentorx.api.feature.job.dto.response.JobResponse;
import com.mentorx.api.feature.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> create(@Valid @RequestBody JobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(jobService.create(request)));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getById(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getById(jobId)));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> update(@PathVariable UUID jobId,
                                                           @RequestBody JobUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(jobService.update(jobId, request)));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID jobId) {
        jobService.delete(jobId);
        return ResponseEntity.ok(ApiResponse.success("Job deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getOpenJobs(
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                jobService.getOpenJobs(jobType, categoryId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getAllJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                jobService.getAllJobs(status, jobType, categoryId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getByClient(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getByClient(clientId, PageRequest.of(page, size))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getByStatus(
            @PathVariable JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getByStatus(status, PageRequest.of(page, size))));
    }

    @PatchMapping("/{jobId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<JobResponse>> updateStatus(
            @PathVariable UUID jobId,
            @RequestParam JobStatus status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Job status updated", jobService.updateStatus(jobId, status, reason)));
    }
}

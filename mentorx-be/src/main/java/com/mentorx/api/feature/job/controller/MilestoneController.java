package com.mentorx.api.feature.job.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.job.dto.request.MilestoneCreateRequest;
import com.mentorx.api.feature.job.dto.response.MilestoneResponse;
import com.mentorx.api.feature.job.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(@Valid @RequestBody MilestoneCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(milestoneService.create(request)));
    }

    @GetMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> getById(@PathVariable UUID milestoneId) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.getById(milestoneId)));
    }

    @PutMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> update(@PathVariable UUID milestoneId,
                                                                 @Valid @RequestBody MilestoneCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.update(milestoneId, request)));
    }

    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID milestoneId) {
        milestoneService.delete(milestoneId);
        return ResponseEntity.ok(ApiResponse.success("Milestone deleted", null));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<Page<MilestoneResponse>>> getByContract(
            @PathVariable UUID contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.getByContract(contractId, PageRequest.of(page, size))));
    }

    @PostMapping("/{milestoneId}/start")
    public ResponseEntity<ApiResponse<MilestoneResponse>> start(@PathVariable UUID milestoneId) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.start(milestoneId)));
    }

    @PostMapping("/{milestoneId}/submit")
    public ResponseEntity<ApiResponse<MilestoneResponse>> submit(
            @PathVariable UUID milestoneId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.submit(milestoneId, notes)));
    }

    @PostMapping("/{milestoneId}/approve")
    public ResponseEntity<ApiResponse<MilestoneResponse>> approve(
            @PathVariable UUID milestoneId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.approve(milestoneId, notes)));
    }

    @PostMapping("/{milestoneId}/request-revision")
    public ResponseEntity<ApiResponse<MilestoneResponse>> requestRevision(
            @PathVariable UUID milestoneId,
            @RequestParam String notes) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.requestRevision(milestoneId, notes)));
    }

    @PostMapping("/{milestoneId}/complete")
    public ResponseEntity<ApiResponse<MilestoneResponse>> complete(
            @PathVariable UUID milestoneId,
            @RequestParam Long transactionId) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.complete(milestoneId, transactionId)));
    }
}

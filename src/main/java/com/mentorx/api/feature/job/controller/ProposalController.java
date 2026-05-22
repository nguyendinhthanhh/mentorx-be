package com.mentorx.api.feature.job.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.job.dto.request.ProposalCreateRequest;
import com.mentorx.api.feature.job.dto.response.ProposalResponse;
import com.mentorx.api.feature.job.service.ProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProposalResponse>> create(@Valid @RequestBody ProposalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(proposalService.create(request)));
    }

    @GetMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getById(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.getById(proposalId)));
    }

    @PutMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> update(@PathVariable UUID proposalId,
                                                                @Valid @RequestBody ProposalCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.update(proposalId, request)));
    }

    @DeleteMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID proposalId) {
        proposalService.delete(proposalId);
        return ResponseEntity.ok(ApiResponse.success("Proposal deleted", null));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> getByJob(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.getByJob(jobId, PageRequest.of(page, size))));
    }

    @GetMapping("/job/{jobId}/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getByJobAndMentor(
            @PathVariable UUID jobId,
            @PathVariable UUID mentorId) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.getByJobAndMentor(jobId, mentorId)));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> getByMentor(
            @PathVariable UUID mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.getByMentor(mentorId, PageRequest.of(page, size))));
    }

    @PostMapping("/{proposalId}/submit")
    public ResponseEntity<ApiResponse<ProposalResponse>> submit(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.submit(proposalId)));
    }

    @PostMapping("/{proposalId}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(@PathVariable UUID proposalId) {
        proposalService.withdraw(proposalId);
        return ResponseEntity.ok(ApiResponse.success("Proposal withdrawn", null));
    }

    @PostMapping("/{proposalId}/accept")
    public ResponseEntity<ApiResponse<ProposalResponse>> accept(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.accept(proposalId)));
    }

    @PostMapping("/{proposalId}/reject")
    public ResponseEntity<ApiResponse<ProposalResponse>> reject(@PathVariable UUID proposalId,
                                                                @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(proposalService.reject(proposalId, reason)));
    }

    @PostMapping("/{proposalId}/view")
    public ResponseEntity<ApiResponse<Void>> markAsViewed(@PathVariable UUID proposalId) {
        proposalService.markAsViewed(proposalId);
        return ResponseEntity.ok(ApiResponse.success("Proposal marked as viewed", null));
    }
}

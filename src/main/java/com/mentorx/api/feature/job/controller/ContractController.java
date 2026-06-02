package com.mentorx.api.feature.job.controller;

import com.mentorx.api.feature.job.enums.ContractStatus;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.job.dto.request.ContractCancellationDecisionRequest;
import com.mentorx.api.feature.job.dto.request.ContractCancellationRequest;
import com.mentorx.api.feature.job.dto.request.ContractCreateRequest;
import com.mentorx.api.feature.job.dto.response.ContractResponse;
import com.mentorx.api.feature.job.service.ContractService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContractResponse>> create(@Valid @RequestBody ContractCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(contractService.create(request)));
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponse>> getById(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getById(contractId)));
    }

    @GetMapping("/mentor/me")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getCurrentMentorContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getCurrentMentorContracts(PageRequest.of(page, size))));
    }

    @GetMapping("/mentor/me/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponse>> getCurrentMentorContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getCurrentMentorContract(contractId)));
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponse>> update(@PathVariable UUID contractId,
                                                                @Valid @RequestBody ContractCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.update(contractId, request)));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getByJob(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getByJob(jobId, PageRequest.of(page, size))));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getByClient(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getByClient(clientId, PageRequest.of(page, size))));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getByMentor(
            @PathVariable UUID mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getByMentor(mentorId, PageRequest.of(page, size))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getByStatus(
            @PathVariable ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getByStatus(status, PageRequest.of(page, size))));
    }

    @PostMapping("/{contractId}/sign/client")
    public ResponseEntity<ApiResponse<ContractResponse>> signByClient(
            @PathVariable UUID contractId,
            @RequestParam String signature,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return ResponseEntity.ok(ApiResponse.success(contractService.signByClient(contractId, signature, ipAddress)));
    }

    @PostMapping("/{contractId}/sign/mentor")
    public ResponseEntity<ApiResponse<ContractResponse>> signByMentor(
            @PathVariable UUID contractId,
            @RequestParam String signature,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return ResponseEntity.ok(ApiResponse.success(contractService.signByMentor(contractId, signature, ipAddress)));
    }

    @PostMapping("/{contractId}/activate")
    public ResponseEntity<ApiResponse<ContractResponse>> activate(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.activate(contractId)));
    }

    @PostMapping("/{contractId}/complete")
    public ResponseEntity<ApiResponse<ContractResponse>> complete(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.complete(contractId)));
    }

    @PostMapping("/{contractId}/cancel")
    public ResponseEntity<ApiResponse<ContractResponse>> cancel(
            @PathVariable UUID contractId,
            @RequestParam UUID userId,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(contractService.cancel(contractId, userId, reason)));
    }

    @PostMapping("/{contractId}/cancellation-request")
    public ResponseEntity<ApiResponse<ContractResponse>> requestCancellation(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractCancellationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.requestCancellation(contractId, request)));
    }

    @PostMapping("/{contractId}/cancellation-request/approve")
    public ResponseEntity<ApiResponse<ContractResponse>> approveCancellation(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractCancellationDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.approveCancellation(contractId, request)));
    }

    @PostMapping("/{contractId}/cancellation-request/reject")
    public ResponseEntity<ApiResponse<ContractResponse>> rejectCancellation(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractCancellationDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.rejectCancellation(contractId, request)));
    }
}

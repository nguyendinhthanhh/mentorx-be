package com.mentorx.api.feature.moderation.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.moderation.dto.request.DisputeCreateRequest;
import com.mentorx.api.feature.moderation.dto.request.DisputeResolveRequest;
import com.mentorx.api.feature.moderation.dto.request.DisputeRespondRequest;
import com.mentorx.api.feature.moderation.dto.response.DisputeResponse;
import com.mentorx.api.feature.moderation.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<ApiResponse<DisputeResponse>> createDispute(@Valid @RequestBody DisputeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(disputeService.createDispute(request)));
    }

    @GetMapping("/{disputeId}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDisputeById(@PathVariable UUID disputeId) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.getDisputeById(disputeId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> getDisputesByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.getDisputesByUser(userId, PageRequest.of(page, size))));
    }

    @PostMapping("/{disputeId}/respond")
    public ResponseEntity<ApiResponse<DisputeResponse>> respondToDispute(
            @PathVariable UUID disputeId,
            @Valid @RequestBody DisputeRespondRequest request) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.respondToDispute(disputeId, request)));
    }

    @PostMapping("/{disputeId}/assign-mediator")
    public ResponseEntity<ApiResponse<DisputeResponse>> assignMediator(
            @PathVariable UUID disputeId,
            @RequestParam UUID mediatorId) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.assignMediator(disputeId, mediatorId)));
    }

    @PostMapping("/{disputeId}/resolve")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @PathVariable UUID disputeId,
            @Valid @RequestBody DisputeResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.resolveDispute(disputeId, request)));
    }
}

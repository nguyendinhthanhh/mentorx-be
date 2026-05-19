package com.mentorx.api.feature.job.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.job.dto.request.NegotiationRequest;
import com.mentorx.api.feature.job.dto.response.NegotiationResponse;
import com.mentorx.api.feature.job.service.ProposalNegotiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/negotiations")
@RequiredArgsConstructor
public class ProposalNegotiationController {

    private final ProposalNegotiationService negotiationService;

    @PostMapping("/client-counter")
    public ResponseEntity<ApiResponse<NegotiationResponse>> clientCounterOffer(
            @Valid @RequestBody NegotiationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(negotiationService.clientCounterOffer(request)));
    }

    @PostMapping("/mentor-counter")
    public ResponseEntity<ApiResponse<NegotiationResponse>> mentorCounterOffer(
            @Valid @RequestBody NegotiationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(negotiationService.mentorCounterOffer(request)));
    }

    @PostMapping("/{negotiationId}/accept")
    public ResponseEntity<ApiResponse<NegotiationResponse>> acceptNegotiation(
            @PathVariable UUID negotiationId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                negotiationService.acceptNegotiation(negotiationId, userId)));
    }

    @PostMapping("/{negotiationId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectNegotiation(
            @PathVariable UUID negotiationId,
            @RequestParam UUID userId) {
        negotiationService.rejectNegotiation(negotiationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Negotiation rejected", null));
    }

    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<ApiResponse<List<NegotiationResponse>>> getByProposal(
            @PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(negotiationService.getByProposal(proposalId)));
    }

    @GetMapping("/proposal/{proposalId}/latest")
    public ResponseEntity<ApiResponse<NegotiationResponse>> getLatestByProposal(
            @PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(negotiationService.getLatestByProposal(proposalId)));
    }
}

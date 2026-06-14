package com.mentorx.api.feature.complaint.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.complaint.dto.request.ComplaintEvidenceCreateRequest;
import com.mentorx.api.feature.complaint.dto.response.ComplaintEvidenceResponse;
import com.mentorx.api.feature.complaint.service.ComplaintEvidenceService;
import com.mentorx.api.common.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints/{complaintId}/evidence")
@RequiredArgsConstructor
public class ComplaintEvidenceController {

    private final ComplaintEvidenceService evidenceService;

    @PostMapping
    @PreAuthorize("@complaintEvaluator.isParty(#complaintId, authentication.principal)")
    public ResponseEntity<ApiResponse<ComplaintEvidenceResponse>> addEvidence(
            @PathVariable UUID complaintId,
            @Valid @RequestBody ComplaintEvidenceCreateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(evidenceService.addEvidence(complaintId, userId, request)));
    }

    @GetMapping
    @PreAuthorize("@complaintEvaluator.isParty(#complaintId, authentication.principal)")
    public ResponseEntity<ApiResponse<List<ComplaintEvidenceResponse>>> getEvidence(@PathVariable UUID complaintId) {
        return ResponseEntity.ok(ApiResponse.success(evidenceService.getEvidenceForComplaint(complaintId)));
    }
}

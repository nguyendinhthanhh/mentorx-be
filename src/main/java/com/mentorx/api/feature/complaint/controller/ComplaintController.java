package com.mentorx.api.feature.complaint.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.complaint.dto.request.ComplaintCreateRequest;
import com.mentorx.api.feature.complaint.dto.request.ComplaintRespondRequest;
import com.mentorx.api.feature.complaint.dto.response.ComplaintResponse;
import com.mentorx.api.feature.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(@Valid @RequestBody ComplaintCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(complaintService.createComplaint(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@complaintEvaluator.isParty(#id, authentication.principal)")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getComplaintById(id)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getComplaintsByUser(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getComplaintsByUser(userId, pageable)));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("@complaintEvaluator.isRespondent(#id, authentication.principal)")
    public ResponseEntity<ApiResponse<ComplaintResponse>> respondToComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintRespondRequest request) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.respondToComplaint(id, request)));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("@complaintEvaluator.isComplainant(#id, authentication.principal)")
    public ResponseEntity<ApiResponse<ComplaintResponse>> withdrawComplaint(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.withdrawComplaint(id)));
    }
}

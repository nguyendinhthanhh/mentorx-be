package com.mentorx.api.feature.job.service;

import com.mentorx.api.feature.job.dto.request.ContractCreateRequest;
import com.mentorx.api.feature.job.dto.response.ContractResponse;
import com.mentorx.api.feature.job.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContractService {
    ContractResponse create(ContractCreateRequest request);
    ContractResponse getById(UUID contractId);
    ContractResponse update(UUID contractId, ContractCreateRequest request);
    Page<ContractResponse> getByJob(UUID jobId, Pageable pageable);
    Page<ContractResponse> getByClient(UUID clientId, Pageable pageable);
    Page<ContractResponse> getByMentor(UUID mentorId, Pageable pageable);
    Page<ContractResponse> getByStatus(ContractStatus status, Pageable pageable);
    
    ContractResponse signByClient(UUID contractId, String signature, String ipAddress);
    ContractResponse signByMentor(UUID contractId, String signature, String ipAddress);
    ContractResponse activate(UUID contractId);
    ContractResponse complete(UUID contractId);
    ContractResponse cancel(UUID contractId, UUID userId, String reason);
}

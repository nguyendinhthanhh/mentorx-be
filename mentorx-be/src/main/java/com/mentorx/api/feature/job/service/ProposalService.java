package com.mentorx.api.feature.job.service;

import com.mentorx.api.feature.job.dto.request.ProposalCreateRequest;
import com.mentorx.api.feature.job.dto.response.ProposalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProposalService {
    ProposalResponse create(ProposalCreateRequest request);
    ProposalResponse getById(UUID proposalId);
    ProposalResponse getByJobAndMentor(UUID jobId, UUID mentorId);
    ProposalResponse update(UUID proposalId, ProposalCreateRequest request);
    void delete(UUID proposalId);
    void withdraw(UUID proposalId);
    Page<ProposalResponse> getByJob(UUID jobId, Pageable pageable);
    Page<ProposalResponse> getByMentor(UUID mentorId, Pageable pageable);
    ProposalResponse submit(UUID proposalId);
    ProposalResponse accept(UUID proposalId);
    ProposalResponse reject(UUID proposalId, String reason);
    void markAsViewed(UUID proposalId);
}

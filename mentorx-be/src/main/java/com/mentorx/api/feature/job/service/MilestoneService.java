package com.mentorx.api.feature.job.service;

import com.mentorx.api.feature.job.dto.request.MilestoneCreateRequest;
import com.mentorx.api.feature.job.dto.response.MilestoneResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilestoneService {
    MilestoneResponse create(MilestoneCreateRequest request);
    MilestoneResponse getById(UUID milestoneId);
    MilestoneResponse update(UUID milestoneId, MilestoneCreateRequest request);
    void delete(UUID milestoneId);
    Page<MilestoneResponse> getByContract(UUID contractId, Pageable pageable);
    
    MilestoneResponse start(UUID milestoneId);
    MilestoneResponse submit(UUID milestoneId, String notes);
    MilestoneResponse approve(UUID milestoneId, String notes);
    MilestoneResponse requestRevision(UUID milestoneId, String notes);
    MilestoneResponse complete(UUID milestoneId, Long transactionId);
}

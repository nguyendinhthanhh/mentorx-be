package com.mentorx.api.feature.moderation.service;

import com.mentorx.api.feature.moderation.dto.request.DisputeCreateRequest;
import com.mentorx.api.feature.moderation.dto.request.DisputeResolveRequest;
import com.mentorx.api.feature.moderation.dto.request.DisputeRespondRequest;
import com.mentorx.api.feature.moderation.dto.response.DisputeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DisputeService {
    DisputeResponse createDispute(DisputeCreateRequest request);
    DisputeResponse getDisputeById(UUID disputeId);
    Page<DisputeResponse> getDisputesByUser(UUID userId, Pageable pageable);
    DisputeResponse respondToDispute(UUID disputeId, DisputeRespondRequest request);
    DisputeResponse assignMediator(UUID disputeId, UUID mediatorId);
    DisputeResponse resolveDispute(UUID disputeId, DisputeResolveRequest request);
}

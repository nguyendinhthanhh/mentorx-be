package com.mentorx.api.feature.matching.service;

import com.mentorx.api.feature.matching.dto.request.MentorMatchScoreRequest;
import com.mentorx.api.feature.matching.dto.response.MentorMatchScoreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MentorMatchScoreService {
    
    MentorMatchScoreResponse create(MentorMatchScoreRequest request);
    
    MentorMatchScoreResponse getById(UUID id);
    
    MentorMatchScoreResponse update(UUID id, MentorMatchScoreRequest request);
    
    void delete(UUID id);
    
    Page<MentorMatchScoreResponse> getAll(Pageable pageable);
    
    Page<MentorMatchScoreResponse> getByUserId(UUID userId, Pageable pageable);
    
    Page<MentorMatchScoreResponse> getByMentorProfileId(UUID mentorProfileId, Pageable pageable);
    
    List<MentorMatchScoreResponse> getTopMatchesForUser(UUID userId, int limit);
    
    void markAsShown(UUID id);
    
    void recomputeExpiredScores();
    
    void computeMatchScore(UUID userId, UUID mentorProfileId);
}

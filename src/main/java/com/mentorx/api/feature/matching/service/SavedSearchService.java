package com.mentorx.api.feature.matching.service;

import com.mentorx.api.feature.matching.dto.request.SavedSearchRequest;
import com.mentorx.api.feature.matching.dto.response.SavedSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SavedSearchService {
    
    SavedSearchResponse create(SavedSearchRequest request);
    
    SavedSearchResponse getById(UUID id);
    
    SavedSearchResponse update(UUID id, SavedSearchRequest request);
    
    void delete(UUID id);
    
    Page<SavedSearchResponse> getAll(Pageable pageable);
    
    List<SavedSearchResponse> getByUserId(UUID userId);
    
    Page<SavedSearchResponse> getByUserIdPaginated(UUID userId, Pageable pageable);
    
    long countByUserId(UUID userId);
}

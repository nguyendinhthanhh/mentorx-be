package com.mentorx.api.feature.system.service;

import com.mentorx.api.feature.system.dto.request.SkillRequest;
import com.mentorx.api.feature.system.dto.response.SkillResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkillService {
    
    SkillResponse create(SkillRequest request);
    
    SkillResponse getById(Integer id);
    
    SkillResponse getBySlug(String slug);
    
    SkillResponse update(Integer id, SkillRequest request);
    
    void delete(Integer id);
    
    Page<SkillResponse> getAll(Pageable pageable);
    
    List<SkillResponse> getAllActive();
    
    List<SkillResponse> searchByName(String query);
    
    void toggleActive(Integer id);
    
    long count();
}

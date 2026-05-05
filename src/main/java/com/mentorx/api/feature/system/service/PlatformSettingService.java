package com.mentorx.api.feature.system.service;

import com.mentorx.api.feature.system.dto.request.PlatformSettingRequest;
import com.mentorx.api.feature.system.dto.response.PlatformSettingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PlatformSettingService {
    
    PlatformSettingResponse create(PlatformSettingRequest request);
    
    PlatformSettingResponse getByKey(String key);
    
    PlatformSettingResponse update(String key, PlatformSettingRequest request);
    
    void delete(String key);
    
    Page<PlatformSettingResponse> getAll(Pageable pageable);
    
    List<PlatformSettingResponse> getAll();
    
    String getValue(String key);
    
    void updateValue(String key, String value, UUID updatedBy);
}

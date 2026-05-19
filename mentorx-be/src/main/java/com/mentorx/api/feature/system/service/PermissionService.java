package com.mentorx.api.feature.system.service;

import com.mentorx.api.feature.system.dto.request.PermissionRequest;
import com.mentorx.api.feature.system.dto.response.PermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    
    PermissionResponse create(PermissionRequest request);
    
    PermissionResponse getById(UUID id);
    
    PermissionResponse getByKey(String permissionKey);
    
    PermissionResponse update(UUID id, PermissionRequest request);
    
    void delete(UUID id);
    
    Page<PermissionResponse> getAll(Pageable pageable);
    
    List<PermissionResponse> getAll();
    
    List<PermissionResponse> getByRoleId(UUID roleId);
    
    void assignToRole(UUID roleId, UUID permissionId);
    
    void removeFromRole(UUID roleId, UUID permissionId);
}

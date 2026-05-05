package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.RolePermission;
import com.mentorx.api.feature.system.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    List<RolePermission> findByRoleId(UUID roleId);
    
    List<RolePermission> findByPermissionId(UUID permissionId);
    
    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
    
    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
    
    void deleteByPermissionId(UUID permissionId);
}

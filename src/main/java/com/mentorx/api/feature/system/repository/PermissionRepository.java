package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByPermissionKey(String permissionKey);
    boolean existsByPermissionKey(String permissionKey);
}

package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.dto.request.PermissionRequest;
import com.mentorx.api.feature.system.dto.response.PermissionResponse;
import com.mentorx.api.feature.system.entity.Permission;
import com.mentorx.api.feature.system.entity.RolePermission;
import com.mentorx.api.feature.system.mapper.SystemMapper;
import com.mentorx.api.feature.system.repository.PermissionRepository;
import com.mentorx.api.feature.system.repository.RolePermissionRepository;
import com.mentorx.api.feature.system.service.PermissionService;
import com.mentorx.api.feature.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final SystemMapper systemMapper;

    @Override
    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        log.info("Creating permission with key: {}", request.permissionKey());

        if (permissionRepository.existsByPermissionKey(request.permissionKey())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Permission with this key already exists");
        }

        Permission entity = systemMapper.toPermission(request);
        entity.setCreatedAt(LocalDateTime.now());

        Permission saved = permissionRepository.save(entity);
        log.info("Created permission with ID: {}", saved.getId());

        return systemMapper.toPermissionResponse(saved);
    }

    @Override
    public PermissionResponse getById(UUID id) {
        log.debug("Fetching permission with ID: {}", id);
        Permission entity = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toPermissionResponse(entity);
    }

    @Override
    public PermissionResponse getByKey(String permissionKey) {
        log.debug("Fetching permission with key: {}", permissionKey);
        Permission entity = permissionRepository.findByPermissionKey(permissionKey)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toPermissionResponse(entity);
    }

    @Override
    @Transactional
    public PermissionResponse update(UUID id, PermissionRequest request) {
        log.info("Updating permission with ID: {}", id);

        Permission entity = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check key uniqueness if changed
        if (request.permissionKey() != null && !request.permissionKey().equals(entity.getPermissionKey())) {
            if (permissionRepository.existsByPermissionKey(request.permissionKey())) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Permission with this key already exists");
            }
        }

        systemMapper.updatePermission(entity, request);

        Permission updated = permissionRepository.save(entity);
        log.info("Updated permission with ID: {}", id);

        return systemMapper.toPermissionResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting permission with ID: {}", id);
        if (!permissionRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        
        // Delete all role-permission associations first
        rolePermissionRepository.deleteByPermissionId(id);
        
        permissionRepository.deleteById(id);
        log.info("Deleted permission with ID: {}", id);
    }

    @Override
    public Page<PermissionResponse> getAll(Pageable pageable) {
        log.debug("Fetching all permissions with pagination");
        return permissionRepository.findAll(pageable)
                .map(systemMapper::toPermissionResponse);
    }

    @Override
    public List<PermissionResponse> getAll() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll().stream()
                .map(systemMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getByRoleId(UUID roleId) {
        log.debug("Fetching permissions for role ID: {}", roleId);
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rp -> permissionRepository.findById(rp.getPermissionId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> systemMapper.toPermissionResponse(opt.get()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignToRole(UUID roleId, UUID permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);

        // Verify role exists
        if (!roleRepository.existsById(roleId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found");
        }

        // Verify permission exists
        if (!permissionRepository.existsById(permissionId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found");
        }

        // Check if already assigned
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Permission already assigned to this role");
        }

        RolePermission rolePermission = RolePermission.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .build();

        rolePermissionRepository.save(rolePermission);
        log.info("Assigned permission {} to role {}", permissionId, roleId);
    }

    @Override
    @Transactional
    public void removeFromRole(UUID roleId, UUID permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not assigned to this role");
        }

        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
        log.info("Removed permission {} from role {}", permissionId, roleId);
    }
}

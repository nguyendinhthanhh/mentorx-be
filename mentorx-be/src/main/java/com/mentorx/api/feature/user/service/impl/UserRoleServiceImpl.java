package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.dto.response.RoleResponse;
import com.mentorx.api.feature.user.dto.response.UserRoleResponse;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.user.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, UUID roleId, UUID grantedBy) {
        log.info("Assigning role {} to user {} by {}", roleId, userId, grantedBy);

        if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "User already has this role");
        }

        User user = findUserById(userId);
        Role role = findRoleById(roleId);
        User granter = findUserById(grantedBy);

        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .user(user)
                .role(role)
                .grantedBy(granter)
                .grantedAt(LocalDateTime.now())
                .build();

        userRoleRepository.save(userRole);
        log.info("Role {} assigned to user {} successfully", roleId, userId);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "User does not have this role");
        }

        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        log.info("Role {} removed from user {} successfully", roleId, userId);
    }

    @Override
    public List<UserRoleResponse> getUserRoles(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);
        return userRoles.stream()
                .map(userMapper::toUserRoleResponse)
                .toList();
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> {
                    RoleResponse response = userMapper.toRoleResponse(role);
                    long userCount = userRoleRepository.countUsersByRoleId(role.getId());
                    return new RoleResponse(
                            response.id(),
                            response.roleName(),
                            response.description(),
                            response.createdAt(),
                            userCount
                    );
                })
                .toList();
    }

    @Override
    public RoleResponse getRoleById(UUID roleId) {
        Role role = findRoleById(roleId);
        RoleResponse response = userMapper.toRoleResponse(role);
        long userCount = userRoleRepository.countUsersByRoleId(roleId);
        
        return new RoleResponse(
                response.id(),
                response.roleName(),
                response.description(),
                response.createdAt(),
                userCount
        );
    }

    @Override
    public RoleResponse getRoleByName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));
        
        RoleResponse response = userMapper.toRoleResponse(role);
        long userCount = userRoleRepository.countUsersByRoleId(role.getId());
        
        return new RoleResponse(
                response.id(),
                response.roleName(),
                response.description(),
                response.createdAt(),
                userCount
        );
    }

    @Override
    public boolean hasRole(UUID userId, String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElse(null);
        if (role == null) {
            return false;
        }
        return userRoleRepository.existsByUserIdAndRoleId(userId, role.getId());
    }

    @Override
    public boolean hasAnyRole(UUID userId, List<String> roleNames) {
        return roleNames.stream()
                .anyMatch(roleName -> hasRole(userId, roleName));
    }

    @Override
    public List<UUID> getUsersWithRole(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));
        
        List<UserRole> userRoles = userRoleRepository.findByRoleId(role.getId());
        return userRoles.stream()
                .map(UserRole::getUserId)
                .toList();
    }

    @Override
    public long countUsersWithRole(UUID roleId) {
        return userRoleRepository.countUsersByRoleId(roleId);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Role findRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));
    }
}
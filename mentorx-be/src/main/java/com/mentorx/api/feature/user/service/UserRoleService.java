package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.response.RoleResponse;
import com.mentorx.api.feature.user.dto.response.UserRoleResponse;

import java.util.List;
import java.util.UUID;

public interface UserRoleService {

    void assignRoleToUser(UUID userId, UUID roleId, UUID grantedBy);

    void removeRoleFromUser(UUID userId, UUID roleId);

    List<UserRoleResponse> getUserRoles(UUID userId);

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(UUID roleId);

    RoleResponse getRoleByName(String roleName);

    boolean hasRole(UUID userId, String roleName);

    boolean hasAnyRole(UUID userId, List<String> roleNames);

    List<UUID> getUsersWithRole(String roleName);

    long countUsersWithRole(UUID roleId);
}
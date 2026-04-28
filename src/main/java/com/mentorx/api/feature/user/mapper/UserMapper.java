package com.mentorx.api.feature.user.mapper;

import com.mentorx.api.feature.user.dto.response.RoleResponse;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.dto.response.UserRoleResponse;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "userRoles")
    @Mapping(target = "mentorProfile", ignore = true) // Ignore for now to avoid circular dependency
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.roleName")
    @Mapping(target = "description", source = "role.description")
    @Mapping(target = "grantedBy", source = "grantedBy.id")
    @Mapping(target = "grantedByName", source = "grantedBy.fullName")
    UserRoleResponse toUserRoleResponse(UserRole userRole);

    @Mapping(target = "userCount", ignore = true)
    RoleResponse toRoleResponse(Role role);

    List<RoleResponse> toRoleResponseList(List<Role> roles);
}
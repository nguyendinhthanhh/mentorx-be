package com.mentorx.api.feature.user.mapper;

import com.mentorx.api.feature.user.dto.response.RoleResponse;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.dto.response.UserRoleResponse;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "mentorStatus", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.normalizeMentorStatus(user.getMentorStatus()))")
    @Mapping(target = "roles", source = "userRoles")
    @Mapping(target = "mentorProfile", ignore = true)
    @Mapping(target = "expertiseStatus", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.determineExpertiseStatus(user))")
    @Mapping(target = "identityStatus", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.determineIdentityStatus(user))")
    @Mapping(target = "payoutStatus", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.determinePayoutStatus(user))")
    @Mapping(target = "verifiedMentorBadge", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.isVerifiedMentorBadge(user))")
    @Mapping(target = "canSwitchToMentorMode", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.canSwitchToMentorMode(user))")
    @Mapping(target = "canRequestWithdrawal", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.canRequestWithdrawal(user))")
    @Mapping(target = "availableModes", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.determineAvailableModes(user))")
    @Mapping(target = "currentMode", expression = "java(com.mentorx.api.feature.user.mapper.UserModeMapper.determineCurrentMode(user))")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.roleName")
    @Mapping(target = "description", source = "role.description")
    @Mapping(target = "grantedBy", source = "grantedBy.id")
    @Mapping(target = "grantedByName", expression = "java(userRole.getGrantedBy() != null ? userRole.getGrantedBy().getFullName() : \"System\")")
    @Mapping(target = "grantedAt", source = "grantedAt")
    UserRoleResponse toUserRoleResponse(UserRole userRole);

    @Mapping(target = "userCount", ignore = true)
    RoleResponse toRoleResponse(Role role);

    List<RoleResponse> toRoleResponseList(List<Role> roles);
}

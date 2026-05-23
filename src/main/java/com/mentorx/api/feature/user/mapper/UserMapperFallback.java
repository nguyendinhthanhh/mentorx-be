package com.mentorx.api.feature.user.mapper;

import com.mentorx.api.feature.user.dto.response.RoleResponse;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.dto.response.UserRoleResponse;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
@ConditionalOnMissingBean(UserMapper.class)
public class UserMapperFallback implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        List<UserRoleResponse> roles = toUserRoleResponseList(user.getUserRoles());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getPhone(),
                user.getCountryCode(),
                user.getPreferredLanguage(),
                user.getStatus(),
                user.getIsEmailVerified(),
                user.getIsMentor(),
                user.getMentorStatus(),
                user.getIs2faEnabled(),
                user.getProfileIsPublic(),
                user.getIsOnboarded(),
                user.getLastSeenAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                roles,
                null,
                UserModeMapper.determineExpertiseStatus(user),
                UserModeMapper.determineIdentityStatus(user),
                UserModeMapper.determinePayoutStatus(user),
                UserModeMapper.isVerifiedMentorBadge(user),
                UserModeMapper.canSwitchToMentorMode(user),
                UserModeMapper.canRequestWithdrawal(user),
                UserModeMapper.determineAvailableModes(user),
                UserModeMapper.determineCurrentMode(user)
        );
    }

    @Override
    public List<UserResponse> toUserResponseList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream().map(this::toUserResponse).collect(Collectors.toList());
    }

    @Override
    public UserRoleResponse toUserRoleResponse(UserRole userRole) {
        if (userRole == null) {
            return null;
        }

        Role role = userRole.getRole();
        User grantedBy = userRole.getGrantedBy();

        return new UserRoleResponse(
                role != null ? role.getId() : null,
                role != null ? role.getRoleName() : null,
                role != null ? role.getDescription() : null,
                grantedBy != null ? grantedBy.getId() : null,
                grantedBy != null ? grantedBy.getFullName() : "System",
                userRole.getGrantedAt()
        );
    }

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.getCreatedAt(),
                null
        );
    }

    @Override
    public List<RoleResponse> toRoleResponseList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream().map(this::toRoleResponse).collect(Collectors.toList());
    }

    private List<UserRoleResponse> toUserRoleResponseList(List<UserRole> userRoles) {
        if (userRoles == null) {
            return null;
        }
        return userRoles.stream().map(this::toUserRoleResponse).collect(Collectors.toList());
    }
}

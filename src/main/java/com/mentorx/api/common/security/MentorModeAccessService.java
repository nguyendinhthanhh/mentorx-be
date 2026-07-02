package com.mentorx.api.common.security;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MentorModeAccessService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public boolean isUser(User user) {
        return user != null;
    }

    public boolean isMentorApproved(User user) {
        return user != null
                && user.getMentorStatus() == MentorStatus.APPROVED
                && hasRole(user, "MENTOR");
    }

    public boolean canAccessMentorMode(User user) {
        return isMentorApproved(user);
    }

    public boolean canManageMentorContent(User user) {
        return isMentorApproved(user);
    }

    public boolean canModerateMentorApplications(User user) {
        return hasRole(user, "ADMIN") || hasRole(user, "MODERATOR");
    }

    public boolean canApproveWithdrawals(User user) {
        return hasRole(user, "ADMIN");
    }

    public void requireSelfOrAdmin(UUID targetUserId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(targetUserId) && !isCurrentUserAdmin()) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot manage another user's account.");
        }
    }

    public void requireApprovedMentorContentAccess(UUID targetUserId) {
        requireSelfOrAdmin(targetUserId);
        if (isCurrentUserAdmin()) {
            return;
        }

        User user = findUserWithRoles(targetUserId);
        if (!canManageMentorContent(user)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Mentor approval is required to access mentor tools.");
        }
    }

    public void requireMentorApplicationModeration() {
        User user = getCurrentUserWithRoles();
        if (!canModerateMentorApplications(user)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Moderator or admin access is required.");
        }
    }

    public UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    public User getCurrentUserWithRoles() {
        return findUserWithRoles(SecurityUtils.getCurrentUserId());
    }

    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public boolean isCurrentUserAdminOrModerator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_MODERATOR".equals(a.getAuthority()));
    }

    private User findUserWithRoles(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setUserRoles(userRoleRepository.findByUserIdWithRole(userId));
        return user;
    }

    private boolean hasRole(User user, String roleName) {
        List<UserRole> roles = user.getUserRoles();
        if (roles == null) {
            return false;
        }
        return roles.stream()
                .map(UserRole::getRole)
                .filter(role -> role != null && role.getRoleName() != null)
                .anyMatch(role -> roleName.equalsIgnoreCase(role.getRoleName()));
    }
}

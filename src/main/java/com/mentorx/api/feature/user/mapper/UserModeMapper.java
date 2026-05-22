package com.mentorx.api.feature.user.mapper;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.UserMode;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;

import java.util.ArrayList;
import java.util.List;

public final class UserModeMapper {

    private UserModeMapper() {
    }

    public static List<UserMode> determineAvailableModes(User user) {
        List<UserMode> modes = new ArrayList<>();
        modes.add(UserMode.USER);
        if (isMentorApproved(user)) {
            modes.add(UserMode.MENTOR);
        }
        return modes;
    }

    public static UserMode determineCurrentMode(User user) {
        return UserMode.USER;
    }

    public static boolean isMentorApproved(User user) {
        if (user == null) {
            return false;
        }
        return normalizeMentorStatus(user.getMentorStatus()) == MentorStatus.APPROVED && hasRole(user, "MENTOR");
    }

    public static MentorStatus normalizeMentorStatus(MentorStatus mentorStatus) {
        if (mentorStatus == null || mentorStatus == MentorStatus.NONE) {
            return MentorStatus.NOT_APPLIED;
        }
        return mentorStatus;
    }

    public static VerificationStatus determineExpertiseStatus(User user) {
        MentorProfile profile = user != null ? user.getMentorProfile() : null;
        if (profile == null || profile.getExpertiseStatus() == null) {
            MentorStatus status = normalizeMentorStatus(user != null ? user.getMentorStatus() : null);
            return switch (status) {
                case APPROVED -> VerificationStatus.APPROVED;
                case PENDING -> VerificationStatus.PENDING;
                case REJECTED, SUSPENDED, REVOKED -> VerificationStatus.REJECTED;
                default -> VerificationStatus.NOT_SUBMITTED;
            };
        }
        return profile.getExpertiseStatus();
    }

    public static VerificationStatus determineIdentityStatus(User user) {
        MentorProfile profile = user != null ? user.getMentorProfile() : null;
        return profile != null && profile.getIdentityStatus() != null
                ? profile.getIdentityStatus()
                : VerificationStatus.NOT_SUBMITTED;
    }

    public static VerificationStatus determinePayoutStatus(User user) {
        MentorProfile profile = user != null ? user.getMentorProfile() : null;
        return profile != null && profile.getPayoutStatus() != null
                ? profile.getPayoutStatus()
                : VerificationStatus.NOT_SUBMITTED;
    }

    public static boolean isVerifiedMentorBadge(User user) {
        return determineIdentityStatus(user) == VerificationStatus.APPROVED;
    }

    public static boolean canSwitchToMentorMode(User user) {
        return determineAvailableModes(user).contains(UserMode.MENTOR);
    }

    public static boolean canRequestWithdrawal(User user) {
        if (!isMentorApproved(user)) {
            return false;
        }
        MentorProfile profile = user != null ? user.getMentorProfile() : null;
        if (profile == null) {
            return false;
        }
        if (determinePayoutStatus(user) != VerificationStatus.APPROVED) {
            return false;
        }
        return !Boolean.TRUE.equals(profile.getIdentityRequired()) || determineIdentityStatus(user) == VerificationStatus.APPROVED;
    }

    private static boolean hasRole(User user, String roleName) {
        if (user.getUserRoles() == null) {
            return false;
        }
        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(role -> role != null && role.getRoleName() != null)
                .anyMatch(role -> roleName.equalsIgnoreCase(role.getRoleName()));
    }
}

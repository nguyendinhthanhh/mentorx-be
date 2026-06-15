package com.mentorx.api.common.util;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Object principal = authentication.getPrincipal();
        
        // Check if principal is CustomUserDetails (which should have userId)
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        
        // Fallback: try to parse username as UUID
        if (principal instanceof UserDetails) {
            try {
                return UUID.fromString(((UserDetails) principal).getUsername());
            } catch (IllegalArgumentException e) {
                // Username is not a UUID (probably email)
                throw new AppException(ErrorCode.ACCESS_DENIED, "Cannot extract user ID from authentication");
            }
        }
        
        throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    /**
     * Returns the current authenticated user id, or {@code null} if the request is anonymous
     * (no principal / not authenticated). Use for endpoints that permit both authenticated and
     * anonymous callers (e.g. analytics view ingestion per DEC-002).
     */
    public static UUID getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        if (principal instanceof UserDetails ud) {
            try {
                return UUID.fromString(ud.getUsername());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}

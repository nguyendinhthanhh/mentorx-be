package com.mentorx.api.feature.user.service;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.feature.user.dto.request.UserCreateRequest;
import com.mentorx.api.feature.user.dto.request.UserUpdateRequest;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(UUID userId);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(UUID userId, UserUpdateRequest request);

    void deleteUser(UUID userId);

    void softDeleteUser(UUID userId);

    UserResponse updateUserStatus(UUID userId, UserStatus status);

    UserResponse updateMentorStatus(UUID userId, MentorStatus mentorStatus);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> getUsersWithFilters(UserStatus status, MentorStatus mentorStatus, 
                                          String searchTerm, Pageable pageable);

    List<UserResponse> searchUsers(String searchQuery);

    long getTotalUsersCount();

    long getActiveUsersCount();

    long getMentorsCount();

    long getPendingMentorApplicationsCount();

    void enable2FA(UUID userId);

    void disable2FA(UUID userId);

    void updateLastSeenAt(UUID userId);

    List<UserResponse> getInactiveUsers(int daysInactive);
}
package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.dto.request.UserCreateRequest;
import com.mentorx.api.feature.user.dto.request.UserUpdateRequest;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("userService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating new user with email: {}", request.email());

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .displayName(request.displayName())
                .bio(request.bio())
                .phone(request.phone())
                .countryCode(request.countryCode())
                .preferredLanguage(request.preferredLanguage() != null ? 
                    request.preferredLanguage() : com.mentorx.api.common.enums.SupportedLanguage.vi)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = findUserByIdAndNotDeleted(userId);
        hydrateRoles(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        hydrateRoles(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", userId);

        User user = findUserByIdAndNotDeleted(userId);

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.countryCode() != null) {
            user.setCountryCode(request.countryCode());
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        if (request.profileIsPublic() != null) {
            user.setProfileIsPublic(request.profileIsPublic());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Hard deleting user with ID: {}", userId);
        User user = findUserByIdAndNotDeleted(userId);
        userRepository.delete(user);
        log.info("User hard deleted: {}", userId);
    }

    @Override
    @Transactional
    public void softDeleteUser(UUID userId) {
        log.info("Soft deleting user with ID: {}", userId);
        User user = findUserByIdAndNotDeleted(userId);
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("User soft deleted: {}", userId);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID userId, UserStatus status) {
        log.info("Updating user status: {} -> {}", userId, status);
        User user = findUserByIdAndNotDeleted(userId);
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        log.info("User status updated: {} -> {}", userId, status);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateMentorStatus(UUID userId, MentorStatus mentorStatus) {
        log.info("Updating mentor status: {} -> {}", userId, mentorStatus);
        User user = findUserByIdAndNotDeleted(userId);
        user.setMentorStatus(mentorStatus);
        user.setIsMentor(mentorStatus == MentorStatus.APPROVED);
        if (mentorStatus == MentorStatus.APPROVED) {
            assignRoleIfMissing(user, "MENTOR");
        }
        User updatedUser = userRepository.save(user);
        hydrateRoles(updatedUser);
        log.info("Mentor status updated: {} -> {}", userId, mentorStatus);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    public Page<UserResponse> getUsersWithFilters(UserStatus status, MentorStatus mentorStatus, 
                                                 String searchTerm, Pageable pageable) {
        return userRepository.findUsersWithFilters(status, mentorStatus, searchTerm, pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    public List<UserResponse> searchUsers(String searchQuery) {
        List<User> users = userRepository.findByFullTextSearch(searchQuery);
        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public long getActiveUsersCount() {
        return userRepository.countByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);
    }

    @Override
    public long getMentorsCount() {
        return userRepository.countByMentorStatusAndDeletedAtIsNull(MentorStatus.APPROVED);
    }

    @Override
    public long getPendingMentorApplicationsCount() {
        return userRepository.countByMentorStatusAndDeletedAtIsNull(MentorStatus.PENDING);
    }

    @Override
    @Transactional
    public void enable2FA(UUID userId) {
        log.info("Enabling 2FA for user: {}", userId);
        User user = findUserByIdAndNotDeleted(userId);
        user.setIs2faEnabled(true);
        // TODO: Generate and set TOTP secret
        userRepository.save(user);
        log.info("2FA enabled for user: {}", userId);
    }

    @Override
    @Transactional
    public void disable2FA(UUID userId) {
        log.info("Disabling 2FA for user: {}", userId);
        User user = findUserByIdAndNotDeleted(userId);
        user.setIs2faEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
        log.info("2FA disabled for user: {}", userId);
    }

    @Override
    @Transactional
    public void updateLastSeenAt(UUID userId) {
        User user = findUserByIdAndNotDeleted(userId);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> getInactiveUsers(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<User> inactiveUsers = userRepository.findInactiveUsers(cutoffDate);
        return inactiveUsers.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    private User findUserByIdAndNotDeleted(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void hydrateRoles(User user) {
        user.setUserRoles(userRoleRepository.findByUserIdWithRole(user.getId()));
    }

    private void assignRoleIfMissing(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found: " + roleName));
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            userRoleRepository.save(UserRole.builder()
                    .userId(user.getId())
                    .roleId(role.getId())
                    .user(user)
                    .role(role)
                    .grantedAt(LocalDateTime.now())
                    .build());
        }
    }
}

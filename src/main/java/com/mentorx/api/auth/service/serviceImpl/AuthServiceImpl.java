package com.mentorx.api.auth.service.serviceImpl;

import com.mentorx.api.auth.dto.request.LoginRequest;
import com.mentorx.api.auth.dto.request.RefreshTokenRequest;
import com.mentorx.api.auth.dto.request.RegisterRequest;
import com.mentorx.api.auth.dto.response.AuthResponse;
import com.mentorx.api.auth.entity.RefreshToken;
import com.mentorx.api.auth.repository.RefreshTokenRepository;
import com.mentorx.api.auth.service.AuthService;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.JwtUtil;
import com.mentorx.api.common.util.HashUtil;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiryMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName((request.firstName() + " " + request.lastName()).trim())
                .displayName(request.firstName())
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .mentorStatus(MentorStatus.NONE)
                .preferredLanguage(SupportedLanguage.vi)
                .build();
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.refreshToken();
        String tokenHash = HashUtil.generateSHA256Hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.getRevokedAt() != null || refreshToken.isExpired() || !jwtUtil.isRefreshToken(rawToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        String accessToken = jwtUtil.generateAccessToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_USER")
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = HashUtil.generateSHA256Hash(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public AuthResponse handleOAuth2Success(String email, String firstName, String lastName, String provider, String providerId) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(email)
                        .fullName((firstName + " " + lastName).trim())
                        .displayName(firstName)
                        .status(UserStatus.ACTIVE)
                        .isEmailVerified(true)
                        .mentorStatus(MentorStatus.NONE)
                        .preferredLanguage(SupportedLanguage.vi)
                        .build())
        );
        return buildAuthResponse(user);
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        log.info("Password reset requested for {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Reset password with token {}", token);
    }

    @Override
    public void sendEmailVerification(String email) {
        log.info("Email verification requested for {}", email);
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Verify email token {}", token);
    }

    @Override
    @Transactional
    public void enable2FA(UUID userId) {
        User user = findUser(userId);
        user.setIs2faEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disable2FA(UUID userId) {
        User user = findUser(userId);
        user.setIs2faEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
    }

    @Override
    public boolean verify2FA(UUID userId, String code) {
        User user = findUser(userId);
        return Boolean.TRUE.equals(user.getIs2faEnabled()) && code != null && code.length() >= 6;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private AuthResponse buildAuthResponse(User user) {
        var principal = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();

        String accessToken = jwtUtil.generateAccessToken(principal);
        String refreshToken = jwtUtil.generateRefreshToken(principal);
        String tokenHash = HashUtil.generateSHA256Hash(refreshToken);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiryMs * 1_000_000L))
                .build());

        UserResponse userResponse = userMapper.toUserResponse(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userResponse)
                .build();
    }
}

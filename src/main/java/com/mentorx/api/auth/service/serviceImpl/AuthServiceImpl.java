package com.mentorx.api.auth.service.serviceImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mentorx.api.auth.dto.request.GithubLoginRequest;
import com.mentorx.api.auth.dto.request.GoogleLoginRequest;
import com.mentorx.api.auth.dto.request.LoginRequest;
import com.mentorx.api.auth.dto.request.RefreshTokenRequest;
import com.mentorx.api.auth.dto.request.RegisterRequest;
import com.mentorx.api.auth.dto.response.AuthResponse;
import com.mentorx.api.auth.dto.response.GithubEmail;
import com.mentorx.api.auth.dto.response.GithubUser;
import com.mentorx.api.auth.entity.RefreshToken;
import com.mentorx.api.auth.repository.RefreshTokenRepository;
import com.mentorx.api.auth.service.AuthService;
import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.SupportedLanguage;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.JwtUtil;
import com.mentorx.api.common.service.EmailService;
import com.mentorx.api.common.util.HashUtil;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.entity.EmailVerificationToken;
import com.mentorx.api.feature.user.entity.PasswordResetToken;
import com.mentorx.api.feature.user.entity.Role;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserRole;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.repository.EmailVerificationTokenRepository;
import com.mentorx.api.feature.user.repository.PasswordResetTokenRepository;
import com.mentorx.api.feature.user.repository.RoleRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import com.mentorx.api.feature.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final WalletService walletService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailDispatcher passwordResetEmailDispatcher;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RestTemplate restTemplate;
    private final EmailService emailService;


    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiryMs;

    @Value("${spring.security.oauth2.client.registration.google.client-id:default-client-id}")
    private String googleClientId;


    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

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
                .status(UserStatus.PENDING)
                .isEmailVerified(false)
                .mentorStatus(MentorStatus.NONE)
                .preferredLanguage(SupportedLanguage.vi)
                .build();
        user = userRepository.save(user);
        assignUserRoleIfMissing(user);

        // Create wallets for new user
        try {
            walletService.createWallet(user.getId(), WalletAccountType.USER_AVAILABLE);
            walletService.createWallet(user.getId(), WalletAccountType.USER_PENDING);
            walletService.createWallet(user.getId(), WalletAccountType.ESCROW);
            log.info("Created wallets for new user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create wallets for user: {}", user.getId(), e);
            // Don't fail registration if wallet creation fails
        }

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
        if (user.getStatus() == UserStatus.PENDING && !user.getIsEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", request.getCredential());
            body.add("client_id", googleClientId);
            body.add("client_secret", googleClientSecret);
            body.add("redirect_uri", "postmessage");
            body.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://oauth2.googleapis.com/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class);

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("Google token exchange returned empty response");
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            String idTokenString = new ObjectMapper().readTree(responseBody).path("id_token").asText(null);
            if (idTokenString == null || idTokenString.isEmpty()) {
                log.error("No id_token in Google token response: {}", responseBody);
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String firstName = (String) payload.get("given_name");
                if (firstName == null) firstName = "User";
                String lastName = (String) payload.get("family_name");
                if (lastName == null) lastName = "";
                String subject = payload.getSubject();

                return handleOAuth2Success(email, firstName, lastName, "google", subject);
            } else {
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login failed", e);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    @Transactional
    public AuthResponse githubLogin(GithubLoginRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "MentorX/1.0");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", githubClientId);
            body.add("client_secret", githubClientSecret);
            body.add("code", request.getCode());

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    "https://github.com/login/oauth/access_token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class);

            String responseBody = rawResponse.getBody();
            log.debug("GitHub token response: {}", responseBody);

            String accessToken = extractAccessToken(responseBody);
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("GitHub OAuth token exchange failed: {}", responseBody);
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            userHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            userHeaders.set("User-Agent", "MentorX/1.0");
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<GithubUser> userResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    userRequest,
                    GithubUser.class);

            GithubUser githubUser = userResponse.getBody();
            if (githubUser == null || githubUser.id() == null) {
                log.error("Failed to fetch GitHub user info");
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            ResponseEntity<GithubEmail[]> emailsResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    userRequest,
                    GithubEmail[].class);

            String email = githubUser.email();
            GithubEmail[] emails = emailsResponse.getBody();
            if ((email == null || email.isEmpty()) && emails != null) {
                email = Arrays.stream(emails)
                        .filter(GithubEmail::primary)
                        .map(GithubEmail::email)
                        .findFirst()
                        .orElse(null);
            }

            if (email == null || email.isEmpty()) {
                log.error("No email found for GitHub user: {}", githubUser.login());
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            String name = githubUser.name();
            if (name == null || name.isEmpty()) {
                name = githubUser.login();
            }

            return handleOAuth2Success(email, name, "", "github", githubUser.id().toString());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("GitHub login failed", e);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
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
                        .password(user.getPasswordHash() != null ? user.getPasswordHash() : "OAUTH2_USER")
                        .authorities(resolveAuthorities(user))
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
        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        if (user == null) {
            user = userRepository.save(User.builder()
                    .email(email)
                    .fullName((firstName + " " + lastName).trim())
                    .displayName(firstName)
                    .status(UserStatus.ACTIVE)
                    .isEmailVerified(true)
                    .mentorStatus(MentorStatus.NONE)
                    .preferredLanguage(SupportedLanguage.vi)
                    .build());
            assignUserRoleIfMissing(user);

            try {
                walletService.createWallet(user.getId(), WalletAccountType.USER_AVAILABLE);
                walletService.createWallet(user.getId(), WalletAccountType.USER_PENDING);
                walletService.createWallet(user.getId(), WalletAccountType.ESCROW);
                log.info("Created wallets for new OAuth2 user: {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to create wallets for OAuth2 user: {}", user.getId(), e);
            }
        }
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "No account was found for this email address."));

        passwordResetEmailDispatcher.validateConfiguration();

        passwordResetTokenRepository.invalidateActiveTokensByUserId(user.getId(), "Superseded by a newer password reset request");

        PasswordResetToken token = PasswordResetToken.createToken(user, user.getEmail());
        token = passwordResetTokenRepository.save(token);

        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token.getToken();
        String recipientName = StringUtils.hasText(user.getDisplayName()) ? user.getDisplayName().trim() : user.getFullName();
        passwordResetEmailDispatcher.sendPasswordResetEmailAsync(user.getEmail(), recipientName, resetUrl);
        log.info("Password reset email queued for {}", user.getEmail());
    }

    @Override
    @Transactional
    public void sendEmailVerification(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getIsEmailVerified()) {
            log.info("Email already verified for user: {}", user.getId());
            return;
        }

        emailVerificationTokenRepository.invalidatePreviousTokens(user.getId());

        EmailVerificationToken verificationToken = EmailVerificationToken.createToken(user, user.getEmail());
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(),
                verificationToken.getToken(), frontendBaseUrl);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        validatePasswordStrength(newPassword);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        resetToken.recordAttempt();

        if (resetToken.hasExceededMaxAttempts()) {
            resetToken.invalidate("Maximum reset attempts exceeded");
            passwordResetTokenRepository.save(resetToken);
            throw new AppException(ErrorCode.INVALID_TOKEN, "This reset link is no longer valid.");
        }

        if (!resetToken.isValid()) {
            passwordResetTokenRepository.save(resetToken);
            if (resetToken.isExpired()) {
                throw new AppException(ErrorCode.TOKEN_EXPIRED, "This reset link has expired. Request a new password reset email.");
            }
            throw new AppException(ErrorCode.INVALID_TOKEN, "This reset link is invalid or has already been used.");
        }

        User user = resetToken.getUser();
        if (StringUtils.hasText(user.getPasswordHash()) && passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Choose a new password that is different from your current password.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.markAsUsed(null, null);
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.invalidateActiveTokensByUserId(user.getId(), "Password changed successfully");
        refreshTokenRepository.revokeAllByUserId(user.getId(), LocalDateTime.now());
        log.info("Password reset completed for user {}", user.getEmail());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));
        
        if (verificationToken.isExpired()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        User user = verificationToken.getUser();
        verificationToken.markAsUsed(null, null);
        user.setIsEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getId());
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

    private String extractAccessToken(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) return null;

        if (responseBody.trim().startsWith("{")) {
            try {
                return new ObjectMapper().readTree(responseBody).path("access_token").asText(null);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse GitHub token response as JSON", e);
                return null;
            }
        }

        for (String pair : responseBody.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "access_token".equals(parts[0])) {
                try {
                    return URLDecoder.decode(parts[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    private AuthResponse buildAuthResponse(User user) {
        user.setUserRoles(userRoleRepository.findByUserIdWithRole(user.getId()));
        var principal = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "OAUTH2_USER")
                .authorities(resolveAuthorities(user))
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

    private void assignUserRoleIfMissing(User user) {
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found: USER"));
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), userRole.getId())) {
            userRoleRepository.save(UserRole.builder()
                    .userId(user.getId())
                    .roleId(userRole.getId())
                    .user(user)
                    .role(userRole)
                    .grantedAt(LocalDateTime.now())
                    .build());
        }
    }

    private String[] resolveAuthorities(User user) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            return new String[]{"ROLE_USER"};
        }

        List<String> authorities = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(role -> role != null && StringUtils.hasText(role.getRoleName()))
                .map(Role::getRoleName)
                .map(String::trim)
                .map(String::toUpperCase)
                .map(roleName -> "ROLE_" + roleName)
                .distinct()
                .toList();

        return authorities.isEmpty()
                ? new String[]{"ROLE_USER"}
                : authorities.toArray(String[]::new);
    }

    private void validatePasswordStrength(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Password must be at least 8 characters long.");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Password must contain at least one uppercase letter.");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Password must contain at least one number.");
        }
    }
    
}

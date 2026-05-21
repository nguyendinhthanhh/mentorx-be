package com.mentorx.api.auth.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mentorx.api.auth.dto.request.GithubLoginRequest;
import com.mentorx.api.auth.dto.request.GoogleLoginRequest;
import com.mentorx.api.auth.dto.request.LoginRequest;
import com.mentorx.api.auth.dto.request.RefreshTokenRequest;
import com.mentorx.api.auth.dto.request.RegisterRequest;
import com.mentorx.api.auth.dto.response.GithubAccessTokenResponse;
import com.mentorx.api.auth.dto.response.GithubEmail;
import com.mentorx.api.auth.dto.response.GithubUser;
import com.mentorx.api.auth.dto.response.AuthResponse;
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
import com.mentorx.api.common.util.HashUtil;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.mapper.UserMapper;
import com.mentorx.api.feature.user.repository.UserRepository;
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
    private final RestTemplate restTemplate;

    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiryMs;

    @Value("${spring.security.oauth2.client.registration.google.client-id:default-client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

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
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getCredential());
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

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", githubClientId);
            body.add("client_secret", githubClientSecret);
            body.add("code", request.getCode());

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);

            ResponseEntity<GithubAccessTokenResponse> tokenResponse = restTemplate.exchange(
                    "https://github.com/login/oauth/access_token",
                    HttpMethod.POST,
                    tokenRequest,
                    GithubAccessTokenResponse.class);

            GithubAccessTokenResponse tokenBody = tokenResponse.getBody();
            if (tokenBody == null || tokenBody.accessToken() == null || tokenBody.accessToken().isEmpty()) {
                log.error("GitHub OAuth token exchange failed: {}", tokenBody);
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(tokenBody.accessToken());
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
    public void devVerifyEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
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
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "OAUTH2_USER")
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

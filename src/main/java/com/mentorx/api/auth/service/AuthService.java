package com.mentorx.api.auth.service;

import com.mentorx.api.auth.dto.request.LoginRequest;
import com.mentorx.api.auth.dto.request.RefreshTokenRequest;
import com.mentorx.api.auth.dto.request.RegisterRequest;
import com.mentorx.api.auth.dto.response.AuthResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(com.mentorx.api.auth.dto.request.GoogleLoginRequest request);

    AuthResponse githubLogin(com.mentorx.api.auth.dto.request.GithubLoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void logoutAll(UUID userId);

    AuthResponse handleOAuth2Success(String email, String firstName, String lastName, 
                                   String provider, String providerId);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword);

    void sendEmailVerification(String email);

    void verifyEmail(String token);

    void changePassword(UUID userId, String currentPassword, String newPassword);

    void enable2FA(UUID userId);

    void disable2FA(UUID userId);

    boolean verify2FA(UUID userId, String code);
}

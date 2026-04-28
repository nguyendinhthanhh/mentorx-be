package com.mentorx.api.auth.service;

import com.mentorx.api.auth.dto.request.LoginRequest;
import com.mentorx.api.auth.dto.request.RefreshTokenRequest;
import com.mentorx.api.auth.dto.request.RegisterRequest;
import com.mentorx.api.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void logoutAll(Long userId);

    AuthResponse handleOAuth2Success(String email, String firstName, String lastName, 
                                   String provider, String providerId);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword);

    void sendEmailVerification(String email);

    void verifyEmail(String token);

    void enable2FA(Long userId);

    void disable2FA(Long userId);

    boolean verify2FA(Long userId, String code);
}
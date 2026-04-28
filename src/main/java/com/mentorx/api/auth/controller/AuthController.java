package com.mentorx.api.auth.controller;

import com.mentorx.api.auth.dto.request.LoginRequest;
import com.mentorx.api.auth.dto.request.RefreshTokenRequest;
import com.mentorx.api.auth.dto.request.RegisterRequest;
import com.mentorx.api.auth.dto.response.AuthResponse;
import com.mentorx.api.auth.service.AuthService;
import com.mentorx.api.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", description = "Revoke all refresh tokens for the user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @Parameter(description = "User ID") @RequestParam Long userId) {
        authService.logoutAll(userId);
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "User email") @RequestParam String email) {
        authService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "Reset token") @RequestParam String token,
            @Parameter(description = "New password") @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    @PostMapping("/send-verification")
    @Operation(summary = "Send email verification", description = "Send email verification link")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(
            @Parameter(description = "User email") @RequestParam String email) {
        authService.sendEmailVerification(email);
        return ResponseEntity.ok(ApiResponse.success("Verification email sent", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email using verification token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Verification token") @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/2fa/enable")
    @Operation(summary = "Enable 2FA", description = "Enable two-factor authentication")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> enable2FA(
            @Parameter(description = "User ID") @RequestParam Long userId) {
        authService.enable2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA enabled successfully", null));
    }

    @PostMapping("/2fa/disable")
    @Operation(summary = "Disable 2FA", description = "Disable two-factor authentication")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> disable2FA(
            @Parameter(description = "User ID") @RequestParam Long userId) {
        authService.disable2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA disabled successfully", null));
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify 2FA code", description = "Verify two-factor authentication code")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> verify2FA(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "2FA code") @RequestParam String code) {
        boolean isValid = authService.verify2FA(userId, code);
        return ResponseEntity.ok(ApiResponse.success("2FA verification completed", isValid));
    }
}
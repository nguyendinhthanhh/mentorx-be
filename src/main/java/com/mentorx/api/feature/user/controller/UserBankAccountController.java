package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.user.dto.request.BankAccountRequest;
import com.mentorx.api.feature.user.dto.response.BankAccountResponse;
import com.mentorx.api.feature.user.service.UserBankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/bank-accounts")
@RequiredArgsConstructor
@Tag(name = "User Bank Accounts", description = "APIs for managing user bank accounts")
public class UserBankAccountController {

    private final UserBankAccountService bankAccountService;

    @PostMapping
    @Operation(summary = "Create bank account", description = "Add a new bank account for user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BankAccountResponse>> create(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse response = bankAccountService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts", description = "Get all bank accounts for user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getAll(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<BankAccountResponse> response = bankAccountService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get bank account by ID", description = "Get specific bank account details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getById(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        BankAccountResponse response = bankAccountService.getById(accountId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default bank account", description = "Get user's default bank account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getDefault(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        BankAccountResponse response = bankAccountService.getDefaultAccount(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update bank account", description = "Update bank account details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BankAccountResponse>> update(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse response = bankAccountService.update(accountId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Bank account updated successfully", response));
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Delete bank account", description = "Delete a bank account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        bankAccountService.delete(accountId, userId);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted successfully", null));
    }

    @PostMapping("/{accountId}/set-default")
    @Operation(summary = "Set as default", description = "Set bank account as default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BankAccountResponse>> setAsDefault(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        BankAccountResponse response = bankAccountService.setAsDefault(accountId, userId);
        return ResponseEntity.ok(ApiResponse.success("Bank account set as default", response));
    }

    @PostMapping("/{accountId}/verify")
    @Operation(summary = "Verify bank account", description = "Verify bank account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BankAccountResponse>> verify(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Parameter(description = "Verified by") @RequestParam(required = false) String verifiedBy) {
        String reviewer = verifiedBy != null ? verifiedBy : SecurityUtils.getCurrentUserId().toString();
        BankAccountResponse response = bankAccountService.verifyAccount(accountId, reviewer);
        return ResponseEntity.ok(ApiResponse.success("Bank account verified successfully", response));
    }

    @PostMapping("/{accountId}/reject")
    @Operation(summary = "Reject payout account", description = "Reject payout setup (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BankAccountResponse>> reject(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Parameter(description = "Reject reason") @RequestParam String reason,
            @Parameter(description = "Rejected by") @RequestParam(required = false) String rejectedBy) {
        String reviewer = rejectedBy != null ? rejectedBy : SecurityUtils.getCurrentUserId().toString();
        BankAccountResponse response = bankAccountService.rejectAccount(accountId, reviewer, reason);
        return ResponseEntity.ok(ApiResponse.success("Bank account rejected", response));
    }

    @GetMapping("/count")
    @Operation(summary = "Count bank accounts", description = "Get count of user's bank accounts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> count(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        long count = bankAccountService.countByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

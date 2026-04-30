package com.mentorx.api.feature.wallet.controller;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.wallet.dto.request.DepositRequest;
import com.mentorx.api.feature.wallet.dto.request.TransferRequest;
import com.mentorx.api.feature.wallet.dto.request.WithdrawalRequest;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import com.mentorx.api.feature.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@PathVariable UUID userId,
                                                                    @RequestParam WalletAccountType accountType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(walletService.createWallet(userId, accountType)));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletById(walletId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserWallets(userId)));
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> deposit(@PathVariable UUID userId,
                                                                          @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.deposit(userId, request)));
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> withdraw(@PathVariable UUID userId,
                                                                           @Valid @RequestBody WithdrawalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.withdraw(userId, request)));
    }

    @PostMapping("/{userId}/transfer")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> transfer(@PathVariable UUID userId,
                                                                           @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.transfer(userId, request)));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getWalletTransactions(@PathVariable UUID walletId,
                                                                                               @RequestParam(defaultValue = "0") int page,
                                                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletTransactions(walletId, PageRequest.of(page, size))));
    }

    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getUserTransactions(@PathVariable UUID userId,
                                                                                             @RequestParam(defaultValue = "0") int page,
                                                                                             @RequestParam(defaultValue = "20") int size,
                                                                                             @RequestParam(required = false) TxnType type) {
        Page<WalletTransactionResponse> data = type == null
                ? walletService.getUserTransactions(userId, PageRequest.of(page, size))
                : walletService.getTransactionsByType(userId, type, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalBalance(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserTotalBalance(userId)));
    }
}

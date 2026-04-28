package com.mentorx.api.feature.wallet.service;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.wallet.dto.request.DepositRequest;
import com.mentorx.api.feature.wallet.dto.request.TransferRequest;
import com.mentorx.api.feature.wallet.dto.request.WithdrawalRequest;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(UUID userId, WalletAccountType accountType);

    WalletResponse getWalletById(UUID walletId);

    WalletResponse getUserWallet(UUID userId, WalletAccountType accountType);

    List<WalletResponse> getUserWallets(UUID userId);

    WalletTransactionResponse deposit(UUID userId, DepositRequest request);

    WalletTransactionResponse withdraw(UUID userId, WithdrawalRequest request);

    WalletTransactionResponse transfer(UUID fromUserId, TransferRequest request);

    WalletTransactionResponse escrowPayment(UUID fromUserId, UUID toUserId, BigDecimal amount, 
                                          UUID referenceId, String referenceType, String description);

    WalletTransactionResponse releaseEscrow(UUID escrowTransactionId, String description);

    WalletTransactionResponse refundEscrow(UUID escrowTransactionId, String description);

    Page<WalletTransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable);

    Page<WalletTransactionResponse> getUserTransactions(UUID userId, Pageable pageable);

    Page<WalletTransactionResponse> getTransactionsByType(UUID userId, TxnType txnType, Pageable pageable);

    WalletTransactionResponse getTransactionById(UUID transactionId);

    List<WalletTransactionResponse> getTransactionsByGroup(UUID transactionGroupId);

    BigDecimal getUserTotalBalance(UUID userId);

    BigDecimal getUserAvailableBalance(UUID userId);

    BigDecimal getUserPendingBalance(UUID userId);

    void freezeWallet(UUID walletId, String reason);

    void unfreezeWallet(UUID walletId);

    boolean validateTransaction(UUID transactionId);

    void reconcileWallet(UUID walletId);

    List<WalletResponse> getWalletsRequiringReconciliation();

    // Internal methods for double-entry bookkeeping
    UUID processDoubleEntryTransaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount,
                                      TxnType txnType, UUID referenceId, String referenceType, String description);

    void processJobPayment(UUID clientId, UUID mentorId, BigDecimal amount, UUID jobId);

    void processJobRelease(UUID mentorId, BigDecimal amount, UUID jobId);

    void processCoursePayment(UUID studentId, UUID mentorId, BigDecimal amount, UUID courseId);

    void processRefund(UUID userId, BigDecimal amount, UUID referenceId, String referenceType);
}
package com.mentorx.api.feature.wallet.service;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.enums.PayoutMethod;
import com.mentorx.api.feature.wallet.dto.request.DepositRequest;
import com.mentorx.api.feature.wallet.dto.request.TransferRequest;
import com.mentorx.api.feature.wallet.dto.request.WithdrawalRequest;
import com.mentorx.api.feature.wallet.dto.response.FinancialSummaryResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {
    FinancialSummaryResponse getFinancialSummary();
    
    Page<com.mentorx.api.feature.wallet.entity.WalletBalanceAuditLog> getAuditLogs(Pageable pageable);

    WalletResponse createWallet(UUID userId, WalletAccountType accountType);

    WalletResponse getWalletById(UUID walletId);

    WalletResponse getUserWallet(UUID userId, WalletAccountType accountType);

    List<WalletResponse> getUserWallets(UUID userId);

    WalletTransactionResponse deposit(UUID userId, DepositRequest request);

    com.mentorx.api.feature.wallet.entity.DepositOrder createDepositOrder(
            UUID userId,
            BigDecimal originalAmount,
            String originalCurrency,
            PaymentGateway gateway,
            String gatewayOrderId,
            String gatewayTxnId,
            String note
    );

    void completeDepositOrder(
            com.mentorx.api.feature.wallet.entity.DepositOrder order,
            String gatewayTxnId,
            String gatewayResponse,
            String note
    );

    void failDepositOrder(
            com.mentorx.api.feature.wallet.entity.DepositOrder order,
            String gatewayTxnId,
            String gatewayResponse,
            String note
    );

    WalletTransactionResponse withdraw(UUID userId, com.mentorx.api.feature.wallet.dto.request.WithdrawalRequest request);

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

    BigDecimal getUserEscrowBalance(UUID userId);

    void freezeWallet(UUID walletId, String reason);

    void unfreezeWallet(UUID walletId);

    boolean validateTransaction(UUID transactionId);

    void reconcileWallet(UUID walletId);

    List<WalletResponse> getWalletsRequiringReconciliation();

    // Internal methods for double-entry bookkeeping
    UUID processDoubleEntryTransaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount,
                                      TxnType txnType, UUID referenceId, String referenceType, String description);

    void depositCallback(com.mentorx.api.feature.wallet.entity.DepositOrder order);

    void processJobPayment(UUID clientId, UUID contractId, BigDecimal totalAmountMxc);

    void releaseMilestone(UUID contractId, UUID milestoneId, BigDecimal amount, BigDecimal platformFee, UUID mentorId);

    void processCoursePurchase(UUID studentId, UUID courseId, UUID instructorId, BigDecimal amount, BigDecimal platformFee);

    com.mentorx.api.feature.wallet.entity.WithdrawalRequest requestWithdrawal(
            UUID userId,
            BigDecimal amount,
            BigDecimal feeAmount,
            String bankName,
            String bankAccountNo,
            String bankAccountName,
            String payoutCountry,
            PayoutMethod payoutMethod,
            String payoutReference
    );

    void completeWithdrawal(UUID requestId, String gatewayTxnId);

    void processRefund(UUID contractId, UUID clientId, BigDecimal refundAmount);

    void addWelcomeBonus(UUID userId, BigDecimal bonusAmount);

    void rejectWithdrawal(UUID requestId, String reason);
}

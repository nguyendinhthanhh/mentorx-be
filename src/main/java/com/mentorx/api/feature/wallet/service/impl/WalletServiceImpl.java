package com.mentorx.api.feature.wallet.service.impl;

import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.util.HashUtil;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.dto.request.DepositRequest;
import com.mentorx.api.feature.wallet.dto.request.TransferRequest;
import com.mentorx.api.feature.wallet.dto.request.WithdrawalRequest;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.entity.WalletTransaction;
import com.mentorx.api.feature.wallet.mapper.WalletMapper;
import com.mentorx.api.feature.wallet.repository.WalletRepository;
import com.mentorx.api.feature.wallet.repository.WalletTransactionRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;

    @Value("${app.wallet.secret-key}")
    private String walletSecretKey;

    @Override
    @Transactional
    public WalletResponse createWallet(UUID userId, WalletAccountType accountType) {
        log.info("Creating wallet for user: {} with type: {}", userId, accountType);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if wallet already exists
        if (walletRepository.findByUserIdAndAccountType(userId, accountType).isPresent()) {
            throw new AppException(ErrorCode.WALLET_ALREADY_EXISTS);
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .accountType(accountType)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created: {}", savedWallet.getId());

        return walletMapper.toWalletResponse(savedWallet);
    }

    @Override
    public WalletResponse getWalletById(UUID walletId) {
        Wallet wallet = findWalletById(walletId);
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public WalletResponse getUserWallet(UUID userId, WalletAccountType accountType) {
        Wallet wallet = walletRepository.findByUserIdAndAccountType(userId, accountType)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public List<WalletResponse> getUserWallets(UUID userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .toList();
    }

    @Override
    @Transactional
    public WalletTransactionResponse deposit(UUID userId, DepositRequest request) {
        log.info("Processing deposit for user: {} amount: {}", userId, request.amount());

        Wallet wallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);

        if (wallet.getIsFrozen()) {
            throw new AppException(ErrorCode.WALLET_FROZEN);
        }

        // Create deposit transaction (single entry for external deposits)
        WalletTransaction transaction = createTransaction(
                wallet, UUID.randomUUID(), TxnType.DEPOSIT, LedgerDirection.CREDIT,
                request.amount(), request.description(), null, "DEPOSIT"
        );

        // Update wallet balance
        wallet.addToBalance(request.amount());
        walletRepository.save(wallet);

        transaction.setStatus(TxnStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        WalletTransaction savedTransaction = transactionRepository.save(transaction);

        log.info("Deposit completed: {}", savedTransaction.getId());
        return walletMapper.toWalletTransactionResponse(savedTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponse withdraw(UUID userId, WithdrawalRequest request) {
        log.info("Processing withdrawal for user: {} amount: {}", userId, request.amount());

        Wallet wallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);

        if (wallet.getIsFrozen()) {
            throw new AppException(ErrorCode.WALLET_FROZEN);
        }

        if (wallet.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Create withdrawal transaction
        WalletTransaction transaction = createTransaction(
                wallet, UUID.randomUUID(), TxnType.WITHDRAWAL, LedgerDirection.DEBIT,
                request.amount(), request.description(), null, "WITHDRAWAL"
        );

        // Update wallet balance
        wallet.subtractFromBalance(request.amount());
        walletRepository.save(wallet);

        transaction.setStatus(TxnStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        WalletTransaction savedTransaction = transactionRepository.save(transaction);

        log.info("Withdrawal completed: {}", savedTransaction.getId());
        return walletMapper.toWalletTransactionResponse(savedTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponse transfer(UUID fromUserId, TransferRequest request) {
        log.info("Processing transfer from: {} to: {} amount: {}", fromUserId, request.toUserId(), request.amount());

        Wallet fromWallet = getOrCreateUserWallet(fromUserId, WalletAccountType.USER_AVAILABLE);
        Wallet toWallet = getOrCreateUserWallet(request.toUserId(), WalletAccountType.USER_AVAILABLE);

        if (fromWallet.getIsFrozen() || toWallet.getIsFrozen()) {
            throw new AppException(ErrorCode.WALLET_FROZEN);
        }

        if (fromWallet.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Process double-entry transaction
        UUID transactionGroupId = processDoubleEntryTransaction(
                fromWallet.getId(), toWallet.getId(), request.amount(),
                TxnType.TRANSFER, null, "TRANSFER", request.description()
        );

        // Return the debit transaction as the primary response
        List<WalletTransaction> transactions = transactionRepository.findByTransactionGroupId(transactionGroupId);
        WalletTransaction debitTransaction = transactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.DEBIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        log.info("Transfer completed: {}", transactionGroupId);
        return walletMapper.toWalletTransactionResponse(debitTransaction);
    }

    @Override
    @Transactional
    public UUID processDoubleEntryTransaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount,
                                            TxnType txnType, UUID referenceId, String referenceType, String description) {
        log.info("Processing double-entry transaction: {} -> {} amount: {}", fromWalletId, toWalletId, amount);

        Wallet fromWallet = findWalletById(fromWalletId);
        Wallet toWallet = findWalletById(toWalletId);

        if (fromWallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        UUID transactionGroupId = UUID.randomUUID();

        // Create debit transaction (from wallet)
        WalletTransaction debitTransaction = createTransaction(
                fromWallet, transactionGroupId, txnType, LedgerDirection.DEBIT,
                amount, description, referenceId, referenceType
        );

        // Create credit transaction (to wallet)
        WalletTransaction creditTransaction = createTransaction(
                toWallet, transactionGroupId, txnType, LedgerDirection.CREDIT,
                amount, description, referenceId, referenceType
        );

        // Update wallet balances
        fromWallet.subtractFromBalance(amount);
        toWallet.addToBalance(amount);

        // Save everything
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        debitTransaction.setStatus(TxnStatus.COMPLETED);
        creditTransaction.setStatus(TxnStatus.COMPLETED);
        debitTransaction.setProcessedAt(LocalDateTime.now());
        creditTransaction.setProcessedAt(LocalDateTime.now());

        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);

        log.info("Double-entry transaction completed: {}", transactionGroupId);
        return transactionGroupId;
    }

    @Override
    @Transactional
    public WalletTransactionResponse escrowPayment(UUID fromUserId, UUID toUserId, BigDecimal amount,
                                                  UUID referenceId, String referenceType, String description) {
        log.info("Processing escrow payment from: {} to: {} amount: {}", fromUserId, toUserId, amount);

        Wallet fromWallet = getOrCreateUserWallet(fromUserId, WalletAccountType.USER_AVAILABLE);
        Wallet escrowWallet = getOrCreateUserWallet(toUserId, WalletAccountType.USER_PENDING);

        UUID transactionGroupId = processDoubleEntryTransaction(
                fromWallet.getId(), escrowWallet.getId(), amount,
                TxnType.JOB_PAYMENT, referenceId, referenceType, description
        );

        List<WalletTransaction> transactions = transactionRepository.findByTransactionGroupId(transactionGroupId);
        WalletTransaction debitTransaction = transactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.DEBIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        log.info("Escrow payment completed: {}", transactionGroupId);
        return walletMapper.toWalletTransactionResponse(debitTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponse releaseEscrow(UUID escrowTransactionId, String description) {
        log.info("Releasing escrow transaction: {}", escrowTransactionId);

        WalletTransaction escrowTransaction = transactionRepository.findById(escrowTransactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (escrowTransaction.getTxnType() != TxnType.JOB_PAYMENT) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }

        // Find the corresponding credit transaction in escrow
        List<WalletTransaction> groupTransactions = transactionRepository
                .findByTransactionGroupId(escrowTransaction.getTransactionGroupId());

        WalletTransaction escrowCredit = groupTransactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        // Move from PENDING to AVAILABLE
        Wallet pendingWallet = escrowCredit.getWallet();
        UUID userId = pendingWallet.getUser().getId();
        Wallet availableWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);

        UUID releaseGroupId = processDoubleEntryTransaction(
                pendingWallet.getId(), availableWallet.getId(), escrowCredit.getAmount(),
                TxnType.JOB_RELEASE, escrowTransaction.getReferenceId(), "JOB_RELEASE", description
        );

        List<WalletTransaction> releaseTransactions = transactionRepository.findByTransactionGroupId(releaseGroupId);
        WalletTransaction releaseTransaction = releaseTransactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        log.info("Escrow released: {}", releaseGroupId);
        return walletMapper.toWalletTransactionResponse(releaseTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponse refundEscrow(UUID escrowTransactionId, String description) {
        log.info("Refunding escrow transaction: {}", escrowTransactionId);

        WalletTransaction escrowTransaction = transactionRepository.findById(escrowTransactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Find original debit transaction to get the original payer
        List<WalletTransaction> groupTransactions = transactionRepository
                .findByTransactionGroupId(escrowTransaction.getTransactionGroupId());

        WalletTransaction originalDebit = groupTransactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.DEBIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        WalletTransaction escrowCredit = groupTransactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        // Refund from escrow back to original payer
        UUID refundGroupId = processDoubleEntryTransaction(
                escrowCredit.getWallet().getId(), originalDebit.getWallet().getId(), escrowCredit.getAmount(),
                TxnType.REFUND, escrowTransaction.getReferenceId(), "REFUND", description
        );

        List<WalletTransaction> refundTransactions = transactionRepository.findByTransactionGroupId(refundGroupId);
        WalletTransaction refundTransaction = refundTransactions.stream()
                .filter(t -> t.getDirection() == LedgerDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));

        log.info("Escrow refunded: {}", refundGroupId);
        return walletMapper.toWalletTransactionResponse(refundTransaction);
    }

    // Additional implementation methods would continue here...
    // For brevity, I'm including the core double-entry bookkeeping logic

    private WalletTransaction createTransaction(Wallet wallet, UUID transactionGroupId, TxnType txnType,
                                              LedgerDirection direction, BigDecimal amount, String description,
                                              UUID referenceId, String referenceType) {
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = direction == LedgerDirection.CREDIT ?
                balanceBefore.add(amount) : balanceBefore.subtract(amount);

        String prevHash = wallet.getLastTransactionHash();
        String entryHash = HashUtil.generateTransactionHash(
                wallet.getId(), txnType, direction, amount, balanceAfter, prevHash, walletSecretKey
        );

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .transactionGroupId(transactionGroupId)
                .txnType(txnType)
                .direction(direction)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .entryHash(entryHash)
                .prevHash(prevHash)
                .build();

        wallet.setLastTransactionHash(entryHash);
        return transaction;
    }

    private Wallet findWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
    }

    private Wallet getOrCreateUserWallet(UUID userId, WalletAccountType accountType) {
        return walletRepository.findByUserIdAndAccountType(userId, accountType)
                .orElseGet(() -> {
                    WalletResponse wallet = createWallet(userId, accountType);
                    return findWalletById(wallet.id());
                });
    }

    // Placeholder implementations for remaining methods
    @Override
    public Page<WalletTransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable) {
        return transactionRepository.findByWalletId(walletId, pageable)
                .map(walletMapper::toWalletTransactionResponse);
    }

    @Override
    public Page<WalletTransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        return transactionRepository.findByWalletUserId(userId, pageable)
                .map(walletMapper::toWalletTransactionResponse);
    }

    @Override
    public Page<WalletTransactionResponse> getTransactionsByType(UUID userId, TxnType txnType, Pageable pageable) {
        return transactionRepository.findByWalletUserIdAndTxnType(userId, txnType, pageable)
                .map(walletMapper::toWalletTransactionResponse);
    }

    @Override
    public WalletTransactionResponse getTransactionById(UUID transactionId) {
        WalletTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        return walletMapper.toWalletTransactionResponse(transaction);
    }

    @Override
    public List<WalletTransactionResponse> getTransactionsByGroup(UUID transactionGroupId) {
        List<WalletTransaction> transactions = transactionRepository.findByTransactionGroupId(transactionGroupId);
        return transactions.stream()
                .map(walletMapper::toWalletTransactionResponse)
                .toList();
    }

    @Override
    public BigDecimal getUserTotalBalance(UUID userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getUserAvailableBalance(UUID userId) {
        return walletRepository.findByUserIdAndAccountType(userId, WalletAccountType.USER_AVAILABLE)
                .map(Wallet::getAvailableBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getUserPendingBalance(UUID userId) {
        return walletRepository.findByUserIdAndAccountType(userId, WalletAccountType.USER_PENDING)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void freezeWallet(UUID walletId, String reason) {
        Wallet wallet = findWalletById(walletId);
        wallet.setIsFrozen(true);
        wallet.setFrozenAt(LocalDateTime.now());
        wallet.setFrozenReason(reason);
        walletRepository.save(wallet);
        log.info("Wallet frozen: {} - {}", walletId, reason);
    }

    @Override
    @Transactional
    public void unfreezeWallet(UUID walletId) {
        Wallet wallet = findWalletById(walletId);
        wallet.setIsFrozen(false);
        wallet.setFrozenAt(null);
        wallet.setFrozenReason(null);
        walletRepository.save(wallet);
        log.info("Wallet unfrozen: {}", walletId);
    }

    @Override
    public boolean validateTransaction(UUID transactionId) {
        // Implementation for transaction validation using hash chain
        return true; // Placeholder
    }

    @Override
    @Transactional
    public void reconcileWallet(UUID walletId) {
        // Implementation for wallet reconciliation
        log.info("Reconciling wallet: {}", walletId);
    }

    @Override
    public List<WalletResponse> getWalletsRequiringReconciliation() {
        // Implementation to find wallets needing reconciliation
        return List.of(); // Placeholder
    }

    @Override
    @Transactional
    public void processJobPayment(UUID clientId, UUID mentorId, BigDecimal amount, UUID jobId) {
        escrowPayment(clientId, mentorId, amount, jobId, "JOB", "Job payment escrow");
    }

    @Override
    @Transactional
    public void processJobRelease(UUID mentorId, BigDecimal amount, UUID jobId) {
        // Find the escrow transaction and release it
        // Implementation would find the specific escrow transaction for this job
        log.info("Processing job release for mentor: {} amount: {} job: {}", mentorId, amount, jobId);
    }

    @Override
    @Transactional
    public void processCoursePayment(UUID studentId, UUID mentorId, BigDecimal amount, UUID courseId) {
        Wallet studentWallet = getOrCreateUserWallet(studentId, WalletAccountType.USER_AVAILABLE);
        Wallet mentorWallet = getOrCreateUserWallet(mentorId, WalletAccountType.USER_AVAILABLE);

        processDoubleEntryTransaction(
                studentWallet.getId(), mentorWallet.getId(), amount,
                TxnType.COURSE_PAYMENT, courseId, "COURSE", "Course payment"
        );
    }

    @Override
    @Transactional
    public void processRefund(UUID userId, BigDecimal amount, UUID referenceId, String referenceType) {
        Wallet wallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);

        createTransaction(
                wallet, UUID.randomUUID(), TxnType.REFUND, LedgerDirection.CREDIT,
                amount, "Refund", referenceId, referenceType
        );

        wallet.addToBalance(amount);
        walletRepository.save(wallet);
    }
}
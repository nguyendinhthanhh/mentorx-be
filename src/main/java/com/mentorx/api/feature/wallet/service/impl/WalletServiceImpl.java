package com.mentorx.api.feature.wallet.service.impl;

import com.mentorx.api.common.enums.LedgerDirection;
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
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (walletRepository.findByUserIdAndAccountType(userId, accountType).isPresent()) {
            throw new AppException(ErrorCode.WALLET_ALREADY_EXISTS);
        }
        Wallet wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .accountType(accountType)
                .build());
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public WalletResponse getWalletById(UUID walletId) {
        return walletMapper.toWalletResponse(findWalletById(walletId));
    }

    @Override
    public WalletResponse getUserWallet(UUID userId, WalletAccountType accountType) {
        Wallet wallet = walletRepository.findByUserIdAndAccountType(userId, accountType)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public List<WalletResponse> getUserWallets(UUID userId) {
        return walletRepository.findByUserId(userId).stream().map(walletMapper::toWalletResponse).toList();
    }

    @Override
    @Transactional
    public WalletTransactionResponse deposit(UUID userId, DepositRequest request) {
        Wallet toWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);
        UUID groupId = UUID.randomUUID();
        WalletTransaction credit = credit(toWallet, groupId, TxnType.DEPOSIT, request.amount(), null, "deposit", request.description());
        walletRepository.save(toWallet);
        transactionRepository.save(credit);
        return walletMapper.toWalletTransactionResponse(credit);
    }

    @Override
    @Transactional
    public WalletTransactionResponse withdraw(UUID userId, WithdrawalRequest request) {
        Wallet fromWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);
        if (fromWallet.getBalanceMxc().compareTo(request.amount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        UUID groupId = UUID.randomUUID();
        WalletTransaction debit = debit(fromWallet, groupId, TxnType.WITHDRAWAL, request.amount(), null, "withdrawal", request.description());
        walletRepository.save(fromWallet);
        transactionRepository.save(debit);
        return walletMapper.toWalletTransactionResponse(debit);
    }

    @Override
    @Transactional
    public WalletTransactionResponse transfer(UUID fromUserId, TransferRequest request) {
        Wallet fromWallet = getOrCreateUserWallet(fromUserId, WalletAccountType.USER_AVAILABLE);
        Wallet toWallet = getOrCreateUserWallet(request.toUserId(), WalletAccountType.USER_AVAILABLE);
        if (fromWallet.getBalanceMxc().compareTo(request.amount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        UUID groupId = processDoubleEntryTransaction(fromWallet.getId(), toWallet.getId(), request.amount(),
                TxnType.ADJUSTMENT, null, "transfer", request.description());
        WalletTransaction tx = transactionRepository.findByTransactionGroupId(groupId).stream()
                .filter(t -> t.getDirection() == LedgerDirection.DEBIT)
                .findFirst().orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));
        return walletMapper.toWalletTransactionResponse(tx);
    }

    @Override
    @Transactional
    public WalletTransactionResponse escrowPayment(UUID fromUserId, UUID toUserId, BigDecimal amount, UUID referenceId, String referenceType, String description) {
        Wallet fromWallet = getOrCreateUserWallet(fromUserId, WalletAccountType.USER_AVAILABLE);
        Wallet escrowWallet = getOrCreateUserWallet(toUserId, WalletAccountType.USER_PENDING);
        UUID groupId = processDoubleEntryTransaction(fromWallet.getId(), escrowWallet.getId(), amount,
                TxnType.JOB_PAYMENT, referenceId, referenceType, description);
        WalletTransaction tx = transactionRepository.findByTransactionGroupId(groupId).stream()
                .filter(t -> t.getDirection() == LedgerDirection.DEBIT)
                .findFirst().orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));
        return walletMapper.toWalletTransactionResponse(tx);
    }

    @Override
    @Transactional
    public WalletTransactionResponse releaseEscrow(UUID escrowTransactionId, String description) {
        WalletTransaction escrowTransaction = transactionRepository.findById(escrowTransactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        UUID userId = escrowTransaction.getWallet().getUser().getId();
        Wallet pendingWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_PENDING);
        Wallet availableWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);

        UUID groupId = processDoubleEntryTransaction(
                pendingWallet.getId(), availableWallet.getId(), escrowTransaction.getAmountMxc(),
                TxnType.JOB_RELEASE, escrowTransaction.getReferenceId(), "job_release", description
        );
        WalletTransaction tx = transactionRepository.findByTransactionGroupId(groupId).stream()
                .filter(t -> t.getDirection() == LedgerDirection.CREDIT)
                .findFirst().orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));
        return walletMapper.toWalletTransactionResponse(tx);
    }

    @Override
    @Transactional
    public WalletTransactionResponse refundEscrow(UUID escrowTransactionId, String description) {
        WalletTransaction escrowTransaction = transactionRepository.findById(escrowTransactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        Wallet pendingWallet = escrowTransaction.getWallet();
        Wallet originalWallet = getOrCreateUserWallet(pendingWallet.getUser().getId(), WalletAccountType.USER_AVAILABLE);
        UUID groupId = processDoubleEntryTransaction(
                pendingWallet.getId(), originalWallet.getId(), escrowTransaction.getAmountMxc(),
                TxnType.JOB_REFUND, escrowTransaction.getReferenceId(), "job_refund", description
        );
        WalletTransaction tx = transactionRepository.findByTransactionGroupId(groupId).stream()
                .filter(t -> t.getDirection() == LedgerDirection.CREDIT)
                .findFirst().orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_FAILED));
        return walletMapper.toWalletTransactionResponse(tx);
    }

    @Override
    public Page<WalletTransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable) {
        return transactionRepository.findByWalletId(walletId, pageable).map(walletMapper::toWalletTransactionResponse);
    }

    @Override
    public Page<WalletTransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        return transactionRepository.findByWalletUserId(userId, pageable).map(walletMapper::toWalletTransactionResponse);
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
        return transactionRepository.findByTransactionGroupId(transactionGroupId).stream()
                .map(walletMapper::toWalletTransactionResponse).toList();
    }

    @Override
    public BigDecimal getUserTotalBalance(UUID userId) {
        return walletRepository.getTotalBalanceByUserId(userId);
    }

    @Override
    public BigDecimal getUserAvailableBalance(UUID userId) {
        return walletRepository.findByUserIdAndAccountType(userId, WalletAccountType.USER_AVAILABLE)
                .map(Wallet::getBalanceMxc)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getUserPendingBalance(UUID userId) {
        return walletRepository.findByUserIdAndAccountType(userId, WalletAccountType.USER_PENDING)
                .map(Wallet::getBalanceMxc)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void freezeWallet(UUID walletId, String reason) {
        log.info("freezeWallet requested for {} with reason {}", walletId, reason);
    }

    @Override
    @Transactional
    public void unfreezeWallet(UUID walletId) {
        log.info("unfreezeWallet requested for {}", walletId);
    }

    @Override
    public boolean validateTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId).isPresent();
    }

    @Override
    @Transactional
    public void reconcileWallet(UUID walletId) {
        log.info("reconcileWallet requested for {}", walletId);
    }

    @Override
    public List<WalletResponse> getWalletsRequiringReconciliation() {
        return List.of();
    }

    @Override
    @Transactional
    public UUID processDoubleEntryTransaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount, TxnType txnType, UUID referenceId, String referenceType, String description) {
        Wallet fromWallet = findWalletById(fromWalletId);
        Wallet toWallet = findWalletById(toWalletId);

        if (fromWallet.getBalanceMxc().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        UUID groupId = UUID.randomUUID();
        WalletTransaction debit = debit(fromWallet, groupId, txnType, amount, referenceId, referenceType, description);
        WalletTransaction credit = credit(toWallet, groupId, txnType, amount, referenceId, referenceType, description);

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        transactionRepository.save(debit);
        transactionRepository.save(credit);
        return groupId;
    }

    @Override
    @Transactional
    public void processJobPayment(UUID clientId, UUID mentorId, BigDecimal amount, UUID jobId) {
        escrowPayment(clientId, mentorId, amount, jobId, "job", "Job payment");
    }

    @Override
    @Transactional
    public void processJobRelease(UUID mentorId, BigDecimal amount, UUID jobId) {
        Wallet pending = getOrCreateUserWallet(mentorId, WalletAccountType.USER_PENDING);
        Wallet available = getOrCreateUserWallet(mentorId, WalletAccountType.USER_AVAILABLE);
        processDoubleEntryTransaction(pending.getId(), available.getId(), amount, TxnType.JOB_RELEASE, jobId, "job", "Job release");
    }

    @Override
    @Transactional
    public void processCoursePayment(UUID studentId, UUID mentorId, BigDecimal amount, UUID courseId) {
        Wallet fromWallet = getOrCreateUserWallet(studentId, WalletAccountType.USER_AVAILABLE);
        Wallet toWallet = getOrCreateUserWallet(mentorId, WalletAccountType.USER_AVAILABLE);
        processDoubleEntryTransaction(fromWallet.getId(), toWallet.getId(), amount, TxnType.COURSE_PURCHASE, courseId, "course", "Course purchase");
    }

    @Override
    @Transactional
    public void processRefund(UUID userId, BigDecimal amount, UUID referenceId, String referenceType) {
        Wallet wallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);
        UUID groupId = UUID.randomUUID();
        WalletTransaction tx = credit(wallet, groupId, TxnType.COURSE_REFUND, amount, referenceId, referenceType, "Refund");
        walletRepository.save(wallet);
        transactionRepository.save(tx);
    }

    private WalletTransaction debit(Wallet wallet, UUID groupId, TxnType type, BigDecimal amount, UUID refId, String refType, String note) {
        wallet.subtractFromBalance(amount);
        return createTransaction(wallet, groupId, type, LedgerDirection.DEBIT, amount, refId, refType, note);
    }

    private WalletTransaction credit(Wallet wallet, UUID groupId, TxnType type, BigDecimal amount, UUID refId, String refType, String note) {
        wallet.addToBalance(amount);
        return createTransaction(wallet, groupId, type, LedgerDirection.CREDIT, amount, refId, refType, note);
    }

    private WalletTransaction createTransaction(Wallet wallet, UUID groupId, TxnType type, LedgerDirection direction,
                                                BigDecimal amount, UUID referenceId, String referenceType, String note) {
        String prevHash = wallet.getLedgerHash();
        String entryHash = HashUtil.generateTransactionHash(
                wallet.getId(), type, direction, amount, wallet.getBalanceMxc(), prevHash, walletSecretKey
        );
        wallet.setLedgerHash(entryHash);
        return WalletTransaction.builder()
                .wallet(wallet)
                .transactionGroupId(groupId)
                .txnType(type)
                .direction(direction)
                .amountMxc(amount)
                .balanceAfterMxc(wallet.getBalanceMxc())
                .referenceId(referenceId)
                .referenceType(referenceType)
                .note(note)
                .entryHash(entryHash)
                .prevEntryHash(prevHash)
                .build();
    }

    private Wallet findWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
    }

    private Wallet getOrCreateUserWallet(UUID userId, WalletAccountType type) {
        return walletRepository.findByUserIdAndAccountType(userId, type).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            return walletRepository.save(Wallet.builder().user(user).accountType(type).build());
        });
    }
}

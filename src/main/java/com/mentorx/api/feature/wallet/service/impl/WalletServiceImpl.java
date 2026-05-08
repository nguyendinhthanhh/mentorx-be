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
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import com.mentorx.api.common.enums.WithdrawalStatus;
import com.mentorx.api.feature.wallet.repository.DepositOrderRepository;
import com.mentorx.api.feature.wallet.repository.WithdrawalRequestRepository;
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
    private final DepositOrderRepository depositOrderRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

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
                .orElseGet(() -> {
                    // Auto-create wallet if not exists
                    log.info("Auto-creating wallet for user {} with type {}", userId, accountType);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                    return walletRepository.save(Wallet.builder()
                            .user(user)
                            .accountType(accountType)
                            .build());
                });
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public List<WalletResponse> getUserWallets(UUID userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        
        // Auto-create missing wallets
        if (wallets.isEmpty() || wallets.size() < 3) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            for (WalletAccountType type : WalletAccountType.values()) {
                boolean exists = wallets.stream()
                        .anyMatch(w -> w.getAccountType() == type);
                if (!exists) {
                    log.info("Auto-creating {} wallet for user {}", type, userId);
                    Wallet newWallet = walletRepository.save(Wallet.builder()
                            .user(user)
                            .accountType(type)
                            .build());
                    wallets.add(newWallet);
                }
            }
        }
        
        return wallets.stream().map(walletMapper::toWalletResponse).toList();
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
    public void depositCallback(DepositOrder order) {
        if (order.getTxnStatus() != com.mentorx.api.common.enums.TxnStatus.PENDING) return;
        
        Wallet platformFloat = getSystemWallet(WalletAccountType.PLATFORM_FLOAT);
        Wallet userAvailable = getOrCreateUserWallet(order.getUser().getId(), WalletAccountType.USER_AVAILABLE);
        
        UUID groupId = UUID.randomUUID();
        
        platformFloat.addToBalance(order.getMxcAmount());
        WalletTransaction t1 = createTransaction(platformFloat, groupId, TxnType.DEPOSIT, LedgerDirection.DEBIT, order.getMxcAmount(), order.getId(), "deposit", "Deposit from " + order.getGateway());
        
        userAvailable.addToBalance(order.getMxcAmount());
        WalletTransaction t2 = createTransaction(userAvailable, groupId, TxnType.DEPOSIT, LedgerDirection.CREDIT, order.getMxcAmount(), order.getId(), "deposit", "Deposit to user");
        
        order.setTxnStatus(com.mentorx.api.common.enums.TxnStatus.COMPLETED);
        walletRepository.save(platformFloat);
        walletRepository.save(userAvailable);
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        depositOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void processJobPayment(UUID clientId, UUID contractId, BigDecimal totalAmountMxc) {
        Wallet clientWallet = getOrCreateUserWallet(clientId, WalletAccountType.USER_AVAILABLE);
        Wallet escrowWallet = getSystemWallet(WalletAccountType.ESCROW);
        processDoubleEntryTransaction(clientWallet.getId(), escrowWallet.getId(), totalAmountMxc, TxnType.JOB_PAYMENT, contractId, "contract", "Lock escrow for contract");
    }

    @Override
    @Transactional
    public void releaseMilestone(UUID contractId, UUID milestoneId, BigDecimal amount, BigDecimal platformFee, UUID mentorId) {
        Wallet escrowWallet = getSystemWallet(WalletAccountType.ESCROW);
        Wallet platformRevenue = getSystemWallet(WalletAccountType.PLATFORM_REVENUE);
        Wallet mentorPending = getOrCreateUserWallet(mentorId, WalletAccountType.USER_PENDING);
        
        UUID groupId = UUID.randomUUID();
        BigDecimal netAmount = amount.subtract(platformFee);
        
        escrowWallet.subtractFromBalance(amount);
        WalletTransaction t1 = createTransaction(escrowWallet, groupId, TxnType.JOB_RELEASE, LedgerDirection.DEBIT, amount, milestoneId, "milestone", "Release milestone");
        transactionRepository.save(t1);
        walletRepository.save(escrowWallet);
        
        if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
            platformRevenue.addToBalance(platformFee);
            WalletTransaction t2 = createTransaction(platformRevenue, groupId, TxnType.PLATFORM_FEE, LedgerDirection.CREDIT, platformFee, milestoneId, "milestone", "Platform fee for milestone");
            transactionRepository.save(t2);
            walletRepository.save(platformRevenue);
        }
        
        mentorPending.addToBalance(netAmount);
        WalletTransaction t3 = createTransaction(mentorPending, groupId, TxnType.JOB_RELEASE, LedgerDirection.CREDIT, netAmount, milestoneId, "milestone", "Mentor net amount");
        transactionRepository.save(t3);
        walletRepository.save(mentorPending);
    }

    @Override
    @Transactional
    public void processCoursePurchase(UUID studentId, UUID courseId, UUID instructorId, BigDecimal amount, BigDecimal platformFee) {
        Wallet studentWallet = getOrCreateUserWallet(studentId, WalletAccountType.USER_AVAILABLE);
        Wallet platformRevenue = getSystemWallet(WalletAccountType.PLATFORM_REVENUE);
        Wallet instructorPending = getOrCreateUserWallet(instructorId, WalletAccountType.USER_PENDING);
        
        UUID groupId = UUID.randomUUID();
        BigDecimal netAmount = amount.subtract(platformFee);
        
        studentWallet.subtractFromBalance(amount);
        WalletTransaction t1 = createTransaction(studentWallet, groupId, TxnType.COURSE_PURCHASE, LedgerDirection.DEBIT, amount, courseId, "course", "Course purchase");
        transactionRepository.save(t1);
        walletRepository.save(studentWallet);
        
        if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
            platformRevenue.addToBalance(platformFee);
            WalletTransaction t2 = createTransaction(platformRevenue, groupId, TxnType.PLATFORM_FEE, LedgerDirection.CREDIT, platformFee, courseId, "course", "Platform fee for course");
            transactionRepository.save(t2);
            walletRepository.save(platformRevenue);
        }
        
        instructorPending.addToBalance(netAmount);
        WalletTransaction t3 = createTransaction(instructorPending, groupId, TxnType.COURSE_PURCHASE, LedgerDirection.CREDIT, netAmount, courseId, "course", "Instructor net amount");
        transactionRepository.save(t3);
        walletRepository.save(instructorPending);
    }

    @Override
    @Transactional
    public com.mentorx.api.feature.wallet.entity.WithdrawalRequest requestWithdrawal(UUID userId, BigDecimal amount, BigDecimal feeAmount, String bankName, String bankAccountNo, String bankAccountName) {
        Wallet userWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);
        Wallet platformRevenue = getSystemWallet(WalletAccountType.PLATFORM_REVENUE);
        Wallet platformFloat = getSystemWallet(WalletAccountType.PLATFORM_FLOAT);
        
        if (userWallet.getBalanceMxc().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        BigDecimal netAmount = amount.subtract(feeAmount);
        com.mentorx.api.feature.wallet.entity.WithdrawalRequest request = com.mentorx.api.feature.wallet.entity.WithdrawalRequest.builder()
                .user(userWallet.getUser())
                .mxcAmount(amount)
                .feeMxc(feeAmount)
                .netMxc(netAmount)
                .bankName(bankName)
                .bankAccountNo(bankAccountNo)
                .bankAccountName(bankAccountName)
                .status(WithdrawalStatus.PENDING)
                .build();
        
        request = withdrawalRequestRepository.save(request);
        UUID groupId = UUID.randomUUID();
        
        userWallet.subtractFromBalance(amount);
        WalletTransaction t1 = createTransaction(userWallet, groupId, TxnType.WITHDRAWAL, LedgerDirection.DEBIT, amount, request.getId(), "withdrawal", "Withdrawal request");
        transactionRepository.save(t1);
        walletRepository.save(userWallet);
        
        if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
            platformRevenue.addToBalance(feeAmount);
            WalletTransaction t2 = createTransaction(platformRevenue, groupId, TxnType.WITHDRAWAL_FEE, LedgerDirection.CREDIT, feeAmount, request.getId(), "withdrawal", "Withdrawal fee");
            transactionRepository.save(t2);
            walletRepository.save(platformRevenue);
        }
        
        platformFloat.addToBalance(netAmount);
        WalletTransaction t3 = createTransaction(platformFloat, groupId, TxnType.WITHDRAWAL, LedgerDirection.CREDIT, netAmount, request.getId(), "withdrawal", "Pending withdrawal");
        transactionRepository.save(t3);
        walletRepository.save(platformFloat);
        
        return request;
    }

    @Override
    @Transactional
    public void completeWithdrawal(UUID requestId, String gatewayTxnId) {
        com.mentorx.api.feature.wallet.entity.WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        
        if (request.getStatus() == WithdrawalStatus.COMPLETED) return;
        
        Wallet platformFloat = getSystemWallet(WalletAccountType.PLATFORM_FLOAT);
        UUID groupId = UUID.randomUUID();
        
        platformFloat.subtractFromBalance(request.getNetMxc());
        WalletTransaction t1 = createTransaction(platformFloat, groupId, TxnType.WITHDRAWAL, LedgerDirection.DEBIT, request.getNetMxc(), request.getId(), "withdrawal", "Withdrawal complete");
        transactionRepository.save(t1);
        walletRepository.save(platformFloat);
        
        request.setStatus(WithdrawalStatus.COMPLETED);
        request.setGatewayTxnId(gatewayTxnId);
        withdrawalRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void processRefund(UUID contractId, UUID clientId, BigDecimal refundAmount) {
        Wallet escrowWallet = getSystemWallet(WalletAccountType.ESCROW);
        Wallet clientWallet = getOrCreateUserWallet(clientId, WalletAccountType.USER_AVAILABLE);
        processDoubleEntryTransaction(escrowWallet.getId(), clientWallet.getId(), refundAmount, TxnType.JOB_REFUND, contractId, "contract", "Refund contract");
    }

    @Override
    @Transactional
    public void addWelcomeBonus(UUID userId, BigDecimal bonusAmount) {
        Wallet platformFloat = getSystemWallet(WalletAccountType.PLATFORM_FLOAT);
        Wallet userWallet = getOrCreateUserWallet(userId, WalletAccountType.USER_AVAILABLE);
        processDoubleEntryTransaction(platformFloat.getId(), userWallet.getId(), bonusAmount, TxnType.BONUS_CREDIT, null, "bonus", "Welcome bonus");
    }

    @Override
    @Transactional
    public void rejectWithdrawal(UUID requestId, String reason) {
        com.mentorx.api.feature.wallet.entity.WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (request.getStatus() != com.mentorx.api.common.enums.WithdrawalStatus.PENDING 
                && request.getStatus() != com.mentorx.api.common.enums.WithdrawalStatus.PROCESSING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Wallet userWallet = getOrCreateUserWallet(request.getUser().getId(), WalletAccountType.USER_AVAILABLE);
        Wallet platformRevenue = getSystemWallet(WalletAccountType.PLATFORM_REVENUE);
        Wallet platformFloat = getSystemWallet(WalletAccountType.PLATFORM_FLOAT);

        UUID groupId = UUID.randomUUID();

        // Hoàn tiền net từ platform float
        platformFloat.subtractFromBalance(request.getNetMxc());
        WalletTransaction t1 = createTransaction(platformFloat, groupId, TxnType.WITHDRAWAL_REFUND, LedgerDirection.DEBIT, request.getNetMxc(), request.getId(), "withdrawal", "Withdrawal reject - refund net");
        transactionRepository.save(t1);
        walletRepository.save(platformFloat);

        // Hoàn fee từ platform revenue
        if (request.getFeeMxc().compareTo(BigDecimal.ZERO) > 0) {
            platformRevenue.subtractFromBalance(request.getFeeMxc());
            WalletTransaction t2 = createTransaction(platformRevenue, groupId, TxnType.WITHDRAWAL_REFUND, LedgerDirection.DEBIT, request.getFeeMxc(), request.getId(), "withdrawal", "Withdrawal reject - refund fee");
            transactionRepository.save(t2);
            walletRepository.save(platformRevenue);
        }

        // Trả lại ví user
        userWallet.addToBalance(request.getMxcAmount());
        WalletTransaction t3 = createTransaction(userWallet, groupId, TxnType.WITHDRAWAL_REFUND, LedgerDirection.CREDIT, request.getMxcAmount(), request.getId(), "withdrawal", "Withdrawal reject - total amount");
        transactionRepository.save(t3);
        walletRepository.save(userWallet);

        request.setStatus(com.mentorx.api.common.enums.WithdrawalStatus.CANCELLED);
        request.setRejectionReason(reason);
        withdrawalRequestRepository.save(request);
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

    private Wallet getSystemWallet(WalletAccountType type) {
        return walletRepository.findByAccountType(type).stream().findFirst()
                .orElseGet(() -> walletRepository.save(Wallet.builder().accountType(type).build()));
    }
}

package com.mentorx.api.feature.wallet.controller;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.common.enums.WithdrawalStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.wallet.dto.request.DepositCreateRequest;
import com.mentorx.api.feature.wallet.dto.request.TransferRequest;
import com.mentorx.api.feature.wallet.dto.request.WithdrawCreateRequest;
import com.mentorx.api.feature.wallet.dto.response.DepositOrderResponse;
import com.mentorx.api.feature.wallet.dto.response.FinancialSummaryResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import com.mentorx.api.feature.wallet.dto.response.WithdrawalResponse;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import com.mentorx.api.feature.wallet.entity.WithdrawalRequest;
import com.mentorx.api.feature.wallet.mapper.WalletMapper;
import com.mentorx.api.feature.wallet.repository.DepositOrderRepository;
import com.mentorx.api.feature.wallet.repository.WithdrawalRequestRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;
    private final DepositOrderRepository depositOrderRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final UserRepository userRepository;

    @Value("${app.wallet.exchange-rate:1000}")
    private BigDecimal exchangeRate;

    @Value("${app.wallet.withdrawal-fee-percent:2}")
    private BigDecimal withdrawalFeePercent;

    // ==================== Wallet Query APIs ====================

    /**
     * Lấy tất cả wallet của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserWallets(userId)));
    }

    /**
     * Lấy wallet theo loại
     */
    @GetMapping("/user/{userId}/type/{accountType}")
    public ResponseEntity<ApiResponse<WalletResponse>> getUserWallet(
            @PathVariable UUID userId, @PathVariable WalletAccountType accountType) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserWallet(userId, accountType)));
    }

    /**
     * Lấy balance tổng hợp của user
     */
    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getUserBalance(@PathVariable UUID userId) {
        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("total", walletService.getUserTotalBalance(userId));
        balances.put("available", walletService.getUserAvailableBalance(userId));
        balances.put("pending", walletService.getUserPendingBalance(userId));
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    // ==================== Deposit APIs (Luồng 1) ====================

    /**
     * POST /api/v1/wallet/deposit
     * Tạo deposit order → trả về thông tin để redirect sang payment gateway
     */
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<DepositOrderResponse>> createDeposit(
            @RequestParam UUID userId,
            @Valid @RequestBody DepositCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BigDecimal mxcAmount = request.amountVnd().divide(exchangeRate, 4, RoundingMode.DOWN);

        DepositOrder order = DepositOrder.builder()
                .user(user)
                .gateway(com.mentorx.api.common.enums.PaymentGateway.valueOf(request.gateway().toUpperCase()))
                .gatewayOrderId(request.gateway().toUpperCase() + "_" + System.currentTimeMillis())
                .realAmount(request.amountVnd())
                .realCurrency("VND")
                .mxcAmount(mxcAmount)
                .exchangeRate(exchangeRate)
                .build();

        order = depositOrderRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit order created", walletMapper.toDepositOrderResponse(order)));
    }

    /**
     * POST /api/v1/wallet/deposit/callback/{gateway}
     * Webhook callback từ payment gateway (VNPAY, etc.)
     * Idempotent: nếu đã xử lý rồi thì bỏ qua
     */
    @PostMapping("/deposit/callback/{gateway}")
    public ResponseEntity<ApiResponse<String>> depositCallback(
            @PathVariable String gateway,
            @RequestParam String gatewayOrderId,
            @RequestParam(required = false) String gatewayTxnId) {

        DepositOrder order = depositOrderRepository
                .findByGatewayAndGatewayOrderId(gateway.toUpperCase(), gatewayOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));

        if (order.getTxnStatus() != com.mentorx.api.common.enums.TxnStatus.PENDING) {
            return ResponseEntity.ok(ApiResponse.success("Already processed"));
        }

        if (gatewayTxnId != null) {
            order.setGatewayTxnId(gatewayTxnId);
        }

        // Thực hiện double-entry ledger: PLATFORM_FLOAT + USER_AVAILABLE
        walletService.depositCallback(order);

        return ResponseEntity.ok(ApiResponse.success("Deposit completed"));
    }

    // ==================== Withdraw APIs (Luồng 5) ====================

    /**
     * POST /api/v1/wallet/withdraw
     * Tạo withdrawal request
     * Trừ MXC ngay từ USER_AVAILABLE → PLATFORM_REVENUE (fee) + PLATFORM_FLOAT (net)
     */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> createWithdrawal(
            @RequestParam UUID userId,
            @Valid @RequestBody WithdrawCreateRequest request) {

        BigDecimal feeAmount = request.mxcAmount()
                .multiply(withdrawalFeePercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.UP);

        WithdrawalRequest withdrawalRequest = walletService.requestWithdrawal(
                userId,
                request.mxcAmount(),
                feeAmount,
                request.bankName(),
                request.bankAccountNo(),
                request.bankAccountName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal request created", walletMapper.toWithdrawalResponse(withdrawalRequest)));
    }

    /**
     * GET /api/v1/wallet/withdraw/{requestId}
     * Xem trạng thái withdrawal request
     */
    @GetMapping("/withdraw/{requestId}")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> getWithdrawalStatus(@PathVariable UUID requestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(walletMapper.toWithdrawalResponse(request)));
    }

    // ==================== Admin Wallet Overview APIs ====================
    
    @GetMapping("/admin/financial-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getFinancialSummary() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getFinancialSummary()));
    }

    @GetMapping("/admin/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<com.mentorx.api.feature.wallet.entity.WalletBalanceAuditLog>>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getAuditLogs(pageable)));
    }

    @PostMapping("/admin/reconcile-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reconcileAll() {
        // In real world, this would trigger an async job to scan all gateways
        return ResponseEntity.ok(ApiResponse.success("Reconciliation job started successfully"));
    }

    // ==================== Admin Withdraw APIs ====================

    /**
     * POST /api/v1/wallet/admin/withdraw/{requestId}/approve
     * Admin approve withdrawal → chuyển khoản VND thật
     */
    @PostMapping("/admin/withdraw/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> approveWithdrawal(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String gatewayTxnId) {

        walletService.completeWithdrawal(requestId, gatewayTxnId != null ? gatewayTxnId : "MANUAL_" + System.currentTimeMillis());

        return ResponseEntity.ok(ApiResponse.success("Withdrawal approved and completed"));
    }

    /**
     * POST /api/v1/wallet/admin/withdraw/{requestId}/reject
     * Admin reject withdrawal → hoàn tiền về USER_AVAILABLE
     */
    @PostMapping("/admin/withdraw/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> rejectWithdrawal(
            @PathVariable UUID requestId,
            @RequestParam String reason) {

        walletService.rejectWithdrawal(requestId, reason);

        return ResponseEntity.ok(ApiResponse.success("Withdrawal rejected and funds returned"));
    }

    // ==================== Transaction History APIs ====================

    /**
     * Lịch sử giao dịch của user
     */
    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getUserTransactions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TxnType type) {

        Page<WalletTransactionResponse> data = type == null
                ? walletService.getUserTransactions(userId, PageRequest.of(page, size))
                : walletService.getTransactionsByType(userId, type, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Chi tiết 1 transaction
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> getTransaction(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getTransactionById(transactionId)));
    }

    /**
     * Lấy transaction group (tất cả entries trong 1 double-entry)
     */
    @GetMapping("/transactions/group/{groupId}")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getTransactionGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getTransactionsByGroup(groupId)));
    }

    @GetMapping("/admin/withdrawals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<WithdrawalResponse>>> listWithdrawals() {
        List<WithdrawalRequest> requests = withdrawalRequestRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(walletMapper.toWithdrawalResponseList(requests)));
    }

    /**
     * POST /api/v1/wallet/transfer
     * Chuyển tiền giữa 2 user (USER_AVAILABLE -> USER_AVAILABLE)
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> transfer(
            @RequestParam UUID fromUserId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.transfer(fromUserId, request)));
    }

    // ==================== Bonus API (Luồng 7) ====================

    /**
     * POST /api/v1/wallet/admin/bonus
     * Tặng bonus MXC cho user (Admin)
     */
    @PostMapping("/admin/bonus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> giveBonus(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount) {
        walletService.addWelcomeBonus(userId, amount);
        return ResponseEntity.ok(ApiResponse.success("Bonus of " + amount + " MXC granted to user"));
    }
}

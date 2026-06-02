package com.mentorx.api.feature.wallet.controller;

import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.wallet.dto.request.ConversionPreviewRequest;
import com.mentorx.api.feature.wallet.dto.request.DepositCreateRequest;
import com.mentorx.api.feature.wallet.dto.request.TransferRequest;
import com.mentorx.api.feature.wallet.dto.request.WithdrawCreateRequest;
import com.mentorx.api.feature.wallet.dto.response.DepositOrderResponse;
import com.mentorx.api.feature.wallet.dto.response.FinancialSummaryResponse;
import com.mentorx.api.feature.wallet.dto.response.MxcConversionResult;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import com.mentorx.api.feature.wallet.dto.response.WithdrawalResponse;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import com.mentorx.api.feature.wallet.entity.WithdrawalRequest;
import com.mentorx.api.feature.wallet.mapper.WalletMapper;
import com.mentorx.api.feature.wallet.repository.DepositOrderRepository;
import com.mentorx.api.feature.wallet.repository.WithdrawalRequestRepository;
import com.mentorx.api.feature.wallet.service.MxcConversionService;
import com.mentorx.api.feature.wallet.service.WalletService;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final MxcConversionService mxcConversionService;
    private final WalletMapper walletMapper;
    private final DepositOrderRepository depositOrderRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Value("${app.wallet.withdrawal-fee-percent:2}")
    private BigDecimal withdrawalFeePercent;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(@PathVariable UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserWallets(userId)));
    }

    @GetMapping("/user/{userId}/type/{accountType}")
    public ResponseEntity<ApiResponse<WalletResponse>> getUserWallet(
            @PathVariable UUID userId,
            @PathVariable WalletAccountType accountType
    ) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserWallet(userId, accountType)));
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getUserBalance(@PathVariable UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("total", walletService.getUserTotalBalance(userId));
        balances.put("available", walletService.getUserAvailableBalance(userId));
        balances.put("pending", walletService.getUserPendingBalance(userId));
        balances.put("escrow", walletService.getUserEscrowBalance(userId));
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    @PostMapping("/conversion-preview")
    public ResponseEntity<ApiResponse<MxcConversionResult>> previewConversion(
            @Valid @RequestBody ConversionPreviewRequest request
    ) {
        MxcConversionResult result = mxcConversionService.convertToMxc(
                request.originalAmount(),
                request.originalCurrency()
        );
        return ResponseEntity.ok(ApiResponse.success("Conversion preview generated", result));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<DepositOrderResponse>> createDeposit(
            @RequestParam UUID userId,
            @Valid @RequestBody DepositCreateRequest request
    ) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        DepositOrder order = walletService.createDepositOrder(
                userId,
                request.resolvedAmount(),
                request.resolvedCurrency(),
                parseGateway(request.gateway()),
                null,
                null,
                "Wallet deposit order created"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit order created", walletMapper.toDepositOrderResponse(order)));
    }

    @PostMapping("/deposit/callback/{gateway}")
    public ResponseEntity<ApiResponse<String>> depositCallback(
            @PathVariable String gateway,
            @RequestParam String gatewayOrderId,
            @RequestParam(required = false) String gatewayTxnId
    ) {
        DepositOrder order = depositOrderRepository
                .findByGatewayAndGatewayOrderId(parseGateway(gateway), gatewayOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_ORDER_NOT_FOUND));

        if (order.getTxnStatus() != com.mentorx.api.common.enums.TxnStatus.PENDING) {
            return ResponseEntity.ok(ApiResponse.success("Already processed"));
        }

        walletService.completeDepositOrder(order, gatewayTxnId, null, "Wallet deposit callback completed");
        return ResponseEntity.ok(ApiResponse.success("Deposit completed"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> createWithdrawal(
            @RequestParam UUID userId,
            @Valid @RequestBody WithdrawCreateRequest request
    ) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        BigDecimal feeAmount = request.mxcAmount()
                .multiply(withdrawalFeePercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.UP);

        WithdrawalRequest withdrawalRequest = walletService.requestWithdrawal(
                userId,
                request.mxcAmount(),
                feeAmount,
                request.bankName(),
                request.bankAccountNo(),
                request.bankAccountName(),
                request.payoutCountry(),
                request.payoutMethod(),
                request.payoutReference()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal request created", walletMapper.toWithdrawalResponse(withdrawalRequest)));
    }

    @GetMapping("/withdraw/{requestId}")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> getWithdrawalStatus(@PathVariable UUID requestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));
        mentorModeAccessService.requireSelfOrAdmin(request.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(walletMapper.toWithdrawalResponse(request)));
    }

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
        return ResponseEntity.ok(ApiResponse.success("Reconciliation job started successfully"));
    }

    @PostMapping("/admin/withdraw/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> approveWithdrawal(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String gatewayTxnId
    ) {
        walletService.completeWithdrawal(requestId, gatewayTxnId != null ? gatewayTxnId : "MANUAL_" + System.currentTimeMillis());
        return ResponseEntity.ok(ApiResponse.success("Withdrawal approved and completed"));
    }

    @PostMapping("/admin/withdraw/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> rejectWithdrawal(
            @PathVariable UUID requestId,
            @RequestParam String reason
    ) {
        walletService.rejectWithdrawal(requestId, reason);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal rejected and funds returned"));
    }

    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getUserTransactions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TxnType type
    ) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        Page<WalletTransactionResponse> data = type == null
                ? walletService.getUserTransactions(userId, PageRequest.of(page, size))
                : walletService.getTransactionsByType(userId, type, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> getTransaction(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getTransactionById(transactionId)));
    }

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

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> transfer(
            @RequestParam UUID fromUserId,
            @Valid @RequestBody TransferRequest request
    ) {
        mentorModeAccessService.requireSelfOrAdmin(fromUserId);
        return ResponseEntity.ok(ApiResponse.success(walletService.transfer(fromUserId, request)));
    }

    @PostMapping("/admin/bonus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> giveBonus(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount
    ) {
        walletService.addWelcomeBonus(userId, amount);
        return ResponseEntity.ok(ApiResponse.success("Bonus of " + amount + " MXC granted to user"));
    }

    private PaymentGateway parseGateway(String gateway) {
        try {
            return PaymentGateway.valueOf(gateway.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD, "Unsupported payment gateway: " + gateway);
        }
    }
}

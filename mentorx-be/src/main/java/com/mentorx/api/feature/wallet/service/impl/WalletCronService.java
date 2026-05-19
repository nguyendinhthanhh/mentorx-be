package com.mentorx.api.feature.wallet.service.impl;

import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.wallet.entity.WalletTransaction;
import com.mentorx.api.feature.wallet.repository.WalletTransactionRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCronService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;

    /**
     * Chạy mỗi giờ (0 * * * * *)
     * Tìm các CREDIT vào USER_PENDING đã đủ 72h và chuyển sang USER_AVAILABLE
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void releasePendingBalances() {
        log.info("Bắt đầu chạy cron job: releasePendingBalances");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(72);
        List<WalletTransaction> releasableTransactions = walletTransactionRepository.findReleasablePendingTransactions(cutoffTime);
        
        if (releasableTransactions.isEmpty()) {
            log.info("Không có giao dịch pending nào cần release.");
            return;
        }

        log.info("Tìm thấy {} giao dịch pending cần release.", releasableTransactions.size());

        for (WalletTransaction pendingCredit : releasableTransactions) {
            try {
                UUID userId = pendingCredit.getWallet().getUser().getId();
                
                // DEBIT USER_PENDING & CREDIT USER_AVAILABLE
                // We use processDoubleEntryTransaction to handle the ledger
                // We set referenceId = pendingCredit.getId() so the query `NOT EXISTS (SELECT 1 FROM WalletTransaction wt2 WHERE wt2.referenceId = wt.id)` will filter it out next time.
                
                WalletTransaction debitTx = pendingCredit;
                
                walletService.processDoubleEntryTransaction(
                        walletService.getUserWallet(userId, WalletAccountType.USER_PENDING).id(),
                        walletService.getUserWallet(userId, WalletAccountType.USER_AVAILABLE).id(),
                        pendingCredit.getAmountMxc(),
                        TxnType.JOB_RELEASE, 
                        pendingCredit.getId(), 
                        "pending_release", 
                        "Auto-release pending balance after 72h"
                );
                
                log.info("Đã release thành công {} MXC cho user {}", pendingCredit.getAmountMxc(), userId);
                // TODO: Gửi notification PAYMENT_RECEIVED
            } catch (Exception e) {
                log.error("Lỗi khi release pending transaction {}: {}", pendingCredit.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Hoàn thành cron job: releasePendingBalances");
    }
}

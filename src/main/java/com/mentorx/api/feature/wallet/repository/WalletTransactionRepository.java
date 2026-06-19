package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;
import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.feature.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByWalletId(UUID walletId);

    Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable);

    List<WalletTransaction> findByTransactionGroupId(UUID transactionGroupId);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.user.id = :userId ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findByWalletUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.user.id = :userId AND wt.txnType = :txnType ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findByWalletUserIdAndTxnType(@Param("userId") UUID userId, @Param("txnType") TxnType txnType, Pageable pageable);

    List<WalletTransaction> findByTxnStatus(TxnStatus status);

    List<WalletTransaction> findByTxnType(TxnType txnType);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.referenceId = :referenceId AND wt.referenceType = :referenceType")
    List<WalletTransaction> findByReferenceIdAndType(@Param("referenceId") UUID referenceId, @Param("referenceType") String referenceType);

    List<WalletTransaction> findByReferenceId(UUID referenceId);

    boolean existsByReferenceIdAndReferenceTypeAndTxnTypeAndDirection(
            UUID referenceId,
            String referenceType,
            TxnType txnType,
            LedgerDirection direction
    );

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.id = :walletId AND wt.txnStatus = :status ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findByWalletIdAndStatus(@Param("walletId") UUID walletId, @Param("status") TxnStatus status);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.createdAt BETWEEN :startDate AND :endDate ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findTransactionsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(wt) FROM WalletTransaction wt WHERE wt.wallet.user.id = :userId AND wt.txnType = :txnType AND wt.txnStatus = 'COMPLETED'")
    long countByUserIdAndTxnType(@Param("userId") UUID userId, @Param("txnType") TxnType txnType);

    @Query("SELECT SUM(wt.amountMxc) FROM WalletTransaction wt WHERE wt.wallet.user.id = :userId AND wt.txnType = :txnType AND wt.txnStatus = 'COMPLETED'")
    java.math.BigDecimal sumAmountByUserIdAndTxnType(@Param("userId") UUID userId, @Param("txnType") TxnType txnType);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.id = :walletId ORDER BY wt.createdAt ASC")
    List<WalletTransaction> findByWalletIdOrderByCreatedAt(@Param("walletId") UUID walletId);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.txnStatus = 'PENDING' AND wt.createdAt < :cutoffDate")
    List<WalletTransaction> findPendingTransactionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT wt.txnType, COUNT(wt) as txnCount FROM WalletTransaction wt WHERE wt.txnStatus = 'COMPLETED' GROUP BY wt.txnType ORDER BY txnCount DESC")
    List<Object[]> getTransactionTypeStats();

    /**
     * Aggregation helper for the analytics job: sum of credits per user for a given
     * {@link TxnType} within a date window, broken out by user. Required by
     * {@code EarningsAggregationJob.aggregateEarnings} (M12 Phase 1.5/1.6).
     * M12.2 H1/H2 verified: pre-existing query restored (was referenced but
     * missing from the repo). M12.2 H2.1 added the DEBIT-side counterpart below.
     */
    @Query("SELECT wt.wallet.user.id, COALESCE(SUM(wt.amountMxc), 0) " +
           "FROM WalletTransaction wt " +
           "WHERE wt.txnType = :txnType " +
           "AND wt.direction = com.mentorx.api.common.enums.LedgerDirection.CREDIT " +
           "AND wt.txnStatus = com.mentorx.api.common.enums.TxnStatus.COMPLETED " +
           "AND wt.createdAt >= :start AND wt.createdAt < :end " +
           "GROUP BY wt.wallet.user.id")
    List<Object[]> sumCreditsByUserInWindow(@Param("txnType") TxnType txnType,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    /**
     * M12.2 Phase H2.1 (L2 + L6 fix): DEBIT-side counterpart of
     * {@link #sumCreditsByUserInWindow}. Server-side aggregation filtered by
     * {@code direction = DEBIT} replaces the previous in-memory walk of all
     * transactions in a date window (the {@code findTransactionsBetweenDates}
     * helper returned all directions/types and filtered in Java).
     */
    @Query("SELECT wt.wallet.user.id, COALESCE(SUM(wt.amountMxc), 0) " +
           "FROM WalletTransaction wt " +
           "WHERE wt.txnType = :txnType " +
           "AND wt.direction = com.mentorx.api.common.enums.LedgerDirection.DEBIT " +
           "AND wt.txnStatus = com.mentorx.api.common.enums.TxnStatus.COMPLETED " +
           "AND wt.createdAt >= :start AND wt.createdAt < :end " +
           "GROUP BY wt.wallet.user.id")
    List<Object[]> sumDebitsByUserInWindow(@Param("txnType") TxnType txnType,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.accountType = 'USER_PENDING' " +
           "AND wt.direction = 'CREDIT' " +
           "AND wt.createdAt <= :cutoffTime " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM WalletTransaction wt2 " +
           "  WHERE wt2.referenceId = wt.id" +
           ")")
    List<WalletTransaction> findReleasablePendingTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);
}

package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.common.enums.TxnType;
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

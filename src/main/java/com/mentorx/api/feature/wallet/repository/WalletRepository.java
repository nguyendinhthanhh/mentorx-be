package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByUserIdAndAccountType(UUID userId, WalletAccountType accountType);

    List<Wallet> findByAccountType(WalletAccountType accountType);

    List<Wallet> findByIsFrozen(Boolean isFrozen);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    List<Wallet> findActiveWalletsByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    BigDecimal getTotalBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(w.availableBalance) FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    BigDecimal getAvailableBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(w.pendingBalance) FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    BigDecimal getPendingBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Wallet w WHERE w.balance != " +
           "(SELECT COALESCE(SUM(CASE WHEN wt.direction = 'CREDIT' THEN wt.amount ELSE -wt.amount END), 0) " +
           "FROM WalletTransaction wt WHERE wt.wallet.id = w.id AND wt.status = 'COMPLETED')")
    List<Wallet> findWalletsRequiringReconciliation();

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.accountType = :accountType")
    long countByAccountType(@Param("accountType") WalletAccountType accountType);

    @Query("SELECT w FROM Wallet w WHERE w.balance > :minBalance ORDER BY w.balance DESC")
    List<Wallet> findWalletsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);
}
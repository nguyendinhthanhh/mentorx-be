package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.common.enums.WalletAccountType;
import com.mentorx.api.feature.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :walletId")
    Optional<Wallet> findByIdForUpdate(@Param("walletId") UUID walletId);

    List<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByUserIdAndAccountType(UUID userId, WalletAccountType accountType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.accountType = :accountType")
    Optional<Wallet> findByUserIdAndAccountTypeForUpdate(@Param("userId") UUID userId, @Param("accountType") WalletAccountType accountType);

    List<Wallet> findByAccountType(WalletAccountType accountType);

    Optional<Wallet> findByUserIsNullAndAccountType(WalletAccountType accountType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user IS NULL AND w.accountType = :accountType")
    Optional<Wallet> findByUserIsNullAndAccountTypeForUpdate(@Param("accountType") WalletAccountType accountType);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.accountType = :accountType")
    long countByAccountType(@Param("accountType") WalletAccountType accountType);

    @Query("SELECT COALESCE(SUM(w.balanceMxc), 0) FROM Wallet w WHERE w.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.isActive = false")
    long countFrozenWallets();

    @Query("SELECT COALESCE(SUM(w.balanceMxc), 0) FROM Wallet w WHERE w.isActive = false")
    BigDecimal getTotalFrozenBalance();

    @Query("SELECT COALESCE(SUM(w.balanceMxc), 0) FROM Wallet w")
    BigDecimal getTotalCirculation();
}

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

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.accountType = :accountType")
    long countByAccountType(@Param("accountType") WalletAccountType accountType);

    @Query("SELECT COALESCE(SUM(w.balanceMxc), 0) FROM Wallet w WHERE w.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") UUID userId);
}

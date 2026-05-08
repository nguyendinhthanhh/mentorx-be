package com.mentorx.api.feature.user.repository;

import com.mentorx.api.feature.user.entity.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBankAccountRepository extends JpaRepository<UserBankAccount, UUID> {

    List<UserBankAccount> findByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    Optional<UserBankAccount> findByUserIdAndIsDefaultTrue(UUID userId);

    Optional<UserBankAccount> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE UserBankAccount b SET b.isDefault = false WHERE b.user.id = :userId")
    void clearDefaultForUser(UUID userId);

    boolean existsByUserIdAndAccountNumber(UUID userId, String accountNumber);

    long countByUserId(UUID userId);
}

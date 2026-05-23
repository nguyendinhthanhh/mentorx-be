package com.mentorx.api.feature.wallet.repository;

import com.mentorx.api.feature.wallet.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    Optional<ExchangeRate> findTopByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByEffectiveAtDescCreatedAtDesc(
            String fromCurrency,
            String toCurrency
    );
}

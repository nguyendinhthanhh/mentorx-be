package com.mentorx.api.feature.wallet.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.wallet.dto.response.FrankfurterRateResponse;
import com.mentorx.api.feature.wallet.entity.ExchangeRate;
import com.mentorx.api.feature.wallet.repository.ExchangeRateRepository;
import com.mentorx.api.feature.wallet.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String BASE_CURRENCY = "VND";

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;

    @Value("${app.exchange-rate.provider-url:https://api.frankfurter.dev/v2/rate}")
    private String providerUrl;

    @Value("${app.exchange-rate.cache-hours:6}")
    private long cacheHours;

    @Value("${app.exchange-rate.source:frankfurter}")
    private String exchangeRateSource;

    @Override
    @Transactional
    public BigDecimal getLatestRateToVnd(String currency) {
        String normalizedCurrency = normalizeCurrency(currency);
        if (BASE_CURRENCY.equals(normalizedCurrency)) {
            return BigDecimal.ONE.setScale(6);
        }

        ExchangeRate latestStoredRate = exchangeRateRepository
                .findTopByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByEffectiveAtDescCreatedAtDesc(
                        normalizedCurrency,
                        BASE_CURRENCY
                )
                .orElse(null);

        if (isFresh(latestStoredRate)) {
            return latestStoredRate.getRate().setScale(6, RoundingMode.HALF_UP);
        }

        try {
            BigDecimal liveRate = fetchLiveRateToVnd(normalizedCurrency);
            ExchangeRate savedRate = exchangeRateRepository.save(ExchangeRate.builder()
                    .fromCurrency(normalizedCurrency)
                    .toCurrency(BASE_CURRENCY)
                    .rate(liveRate)
                    .source(exchangeRateSource)
                    .effectiveAt(LocalDateTime.now())
                    .build());
            return savedRate.getRate().setScale(6, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            if (latestStoredRate != null) {
                log.warn("Using cached exchange rate for {} after live fetch failed: {}", normalizedCurrency, ex.getMessage());
                return latestStoredRate.getRate().setScale(6, RoundingMode.HALF_UP);
            }
            throw new AppException(
                    ErrorCode.BAD_REQUEST,
                    "Unable to retrieve exchange rate for currency: " + normalizedCurrency,
                    ex
            );
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Currency is required");
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isFresh(ExchangeRate exchangeRate) {
        if (exchangeRate == null || exchangeRate.getEffectiveAt() == null) {
            return false;
        }
        return Duration.between(exchangeRate.getEffectiveAt(), LocalDateTime.now()).toHours() < cacheHours;
    }

    private BigDecimal fetchLiveRateToVnd(String currency) {
        String url = providerUrl + "/" + currency + "/" + BASE_CURRENCY;
        try {
            FrankfurterRateResponse response = restTemplate.getForObject(url, FrankfurterRateResponse.class);
            if (response == null || response.rate() == null || response.rate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Exchange rate provider returned an invalid rate for " + currency);
            }
            return response.rate().setScale(6, RoundingMode.HALF_UP);
        } catch (RestClientException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Exchange rate provider request failed for " + currency, ex);
        }
    }
}

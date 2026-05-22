package com.mentorx.api.feature.wallet.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.wallet.dto.response.MxcConversionResult;
import com.mentorx.api.feature.wallet.service.ExchangeRateService;
import com.mentorx.api.feature.wallet.service.MxcConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MxcConversionServiceImpl implements MxcConversionService {

    private static final BigDecimal MXC_TO_VND_RATE = BigDecimal.valueOf(1000);
    private static final String BASE_CURRENCY = "VND";

    private final ExchangeRateService exchangeRateService;

    @Override
    public MxcConversionResult convertToMxc(BigDecimal originalAmount, String originalCurrency) {
        validateOriginalAmount(originalAmount);

        String normalizedCurrency = normalizeCurrency(originalCurrency);
        BigDecimal normalizedAmount = originalAmount.setScale(6, RoundingMode.HALF_UP);
        BigDecimal exchangeRateToVnd = exchangeRateService.getLatestRateToVnd(normalizedCurrency)
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal convertedAmountVnd = BASE_CURRENCY.equals(normalizedCurrency)
                ? normalizedAmount.setScale(2, RoundingMode.HALF_UP)
                : normalizedAmount.multiply(exchangeRateToVnd).setScale(2, RoundingMode.HALF_UP);

        BigDecimal amountMxc = convertedAmountVnd.divide(MXC_TO_VND_RATE, 3, RoundingMode.DOWN);
        if (amountMxc.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_AMOUNT, "Converted MXC amount must be greater than 0");
        }

        return new MxcConversionResult(
                normalizedAmount,
                normalizedCurrency,
                exchangeRateToVnd,
                convertedAmountVnd,
                amountMxc
        );
    }

    private void validateOriginalAmount(BigDecimal originalAmount) {
        if (originalAmount == null) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_AMOUNT, "Original amount is required");
        }
        if (originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_AMOUNT, "Original amount must be greater than 0");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return BASE_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}

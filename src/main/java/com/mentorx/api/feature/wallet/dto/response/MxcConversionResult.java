package com.mentorx.api.feature.wallet.dto.response;

import java.math.BigDecimal;

public record MxcConversionResult(
        BigDecimal originalAmount,
        String originalCurrency,
        BigDecimal exchangeRateToVnd,
        BigDecimal convertedAmountVnd,
        BigDecimal amountMxc
) {
}

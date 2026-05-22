package com.mentorx.api.feature.wallet.dto.response;

import java.math.BigDecimal;

public record FrankfurterRateResponse(
        String date,
        String base,
        String quote,
        BigDecimal rate
) {
}

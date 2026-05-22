package com.mentorx.api.feature.wallet.service;

import java.math.BigDecimal;

public interface ExchangeRateService {

    BigDecimal getLatestRateToVnd(String currency);
}

package com.mentorx.api.feature.wallet.service;

import com.mentorx.api.feature.wallet.dto.response.MxcConversionResult;

import java.math.BigDecimal;

public interface MxcConversionService {

    MxcConversionResult convertToMxc(BigDecimal originalAmount, String originalCurrency);
}

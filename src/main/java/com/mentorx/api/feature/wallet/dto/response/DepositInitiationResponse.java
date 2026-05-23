package com.mentorx.api.feature.wallet.dto.response;

public record DepositInitiationResponse(
        DepositOrderResponse order,
        String paymentUrl,
        String qrCodeUrl,
        String deeplink,
        String message
) {
}

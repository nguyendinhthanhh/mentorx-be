package com.mentorx.api.feature.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for wallet balance
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {
    
    /**
     * Current MX Credits balance
     */
    private BigDecimal balance;
    
    /**
     * Currency code (always "MXC" for MX Credits)
     */
    private String currency;
    
    /**
     * Pending balance (not yet available)
     */
    private BigDecimal pendingBalance;
    
    /**
     * Available balance (can be withdrawn)
     */
    private BigDecimal availableBalance;
}

package com.mentorx.api.feature.wallet.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.common.enums.PaymentGateway;
import com.mentorx.api.common.enums.TxnStatus;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_orders", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"gateway", "gateway_order_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentGateway gateway;

    @Column(name = "gateway_order_id", length = 255)
    private String gatewayOrderId;

    @Column(name = "gateway_txn_id", length = 255)
    private String gatewayTxnId;

    @Column(name = "real_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal realAmount;

    @Column(name = "real_currency", nullable = false, length = 3)
    @Builder.Default
    private String realCurrency = "VND";

    @Column(name = "mxc_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal mxcAmount;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "converted_amount_vnd", nullable = false, precision = 19, scale = 2)
    private BigDecimal convertedAmountVnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_status", nullable = false)
    @Builder.Default
    private TxnStatus txnStatus = TxnStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "JSONB")
    private String gatewayResponse;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;
}

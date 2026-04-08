package com.upes.campusdelivery.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionView(
    Long transactionId,
    String transactionType,
    String paymentSource,
    BigDecimal amount,
    String reason,
    Long orderId,
    Instant createdAt
) {}

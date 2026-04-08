package com.upes.campusdelivery.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateOrderResponse(
    Long orderId,
    String status,
    BigDecimal platformDiscountAmount,
    BigDecimal clusterDiscountAmount,
    BigDecimal totalDiscountAmount,
    BigDecimal finalPayable,
    BigDecimal walletBalanceAfterDebit,
    Instant createdAt,
    boolean idempotentReplay,
    boolean clusterDiscountApplied,
    String clusterWindowKey
) {}

package com.upes.campusdelivery.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(
    Long orderId,
    String status,
    String zoneName,
    BigDecimal subtotalAmount,
    BigDecimal platformDiscountAmount,
    BigDecimal clusterDiscountAmount,
    BigDecimal discountAmount,
    BigDecimal finalPayable,
    BigDecimal walletBalanceAfterDebit,
    Instant createdAt,
    List<OrderItemView> items,
    boolean clusterDiscountApplied,
    String clusterWindowKey
) {}

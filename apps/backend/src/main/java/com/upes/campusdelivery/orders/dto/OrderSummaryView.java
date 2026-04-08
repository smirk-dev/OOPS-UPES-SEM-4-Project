package com.upes.campusdelivery.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryView(
    Long orderId,
    String status,
    String zoneName,
    BigDecimal subtotalAmount,
    BigDecimal discountAmount,
    BigDecimal finalPayable,
    Integer itemCount,
    Instant createdAt
) {}

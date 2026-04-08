package com.upes.campusdelivery.orders.dto;

import java.math.BigDecimal;

public record OrderItemView(
    Long productId,
    String productName,
    BigDecimal mrp,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal lineTotal
) {}

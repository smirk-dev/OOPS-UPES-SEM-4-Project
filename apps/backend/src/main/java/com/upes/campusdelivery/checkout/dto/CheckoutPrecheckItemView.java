package com.upes.campusdelivery.checkout.dto;

import java.math.BigDecimal;

public record CheckoutPrecheckItemView(
    Long productId,
    String productName,
    String stockStatus,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {}

package com.upes.campusdelivery.vendor.dto;

import java.math.BigDecimal;

public record VendorOrderItemView(
    Long productId,
    String productName,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal lineTotal
) {}

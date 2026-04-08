package com.upes.campusdelivery.vendor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VendorProductResponse(
    Long id,
    String name,
    String category,
    String vertical,
    BigDecimal mrp,
    BigDecimal currentPrice,
    BigDecimal savings,
    String stockStatus,
    boolean active,
    BigDecimal flashDiscountPercent,
    Instant updatedAt
) {}

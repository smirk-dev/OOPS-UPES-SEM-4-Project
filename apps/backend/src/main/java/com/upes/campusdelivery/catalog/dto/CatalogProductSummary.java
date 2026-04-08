package com.upes.campusdelivery.catalog.dto;

import java.math.BigDecimal;

public record CatalogProductSummary(
    Long id,
    String name,
    String category,
    String vertical,
    BigDecimal mrp,
    BigDecimal currentPrice,
    BigDecimal savings,
    String stockStatus,
    BigDecimal flashDiscountPercent,
    String vendorShopName
) {}

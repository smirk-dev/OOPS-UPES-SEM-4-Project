package com.upes.campusdelivery.catalog.dto;

import java.math.BigDecimal;

public record CatalogProductDetail(
    Long id,
    String name,
    String description,
    String category,
    String vertical,
    BigDecimal mrp,
    BigDecimal currentPrice,
    BigDecimal savings,
    String stockStatus,
    BigDecimal flashDiscountPercent,
    boolean active,
    String vendorShopName
) {}

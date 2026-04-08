package com.upes.campusdelivery.vendor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VendorOrderSummary(
    Long orderId,
    String status,
    String zoneName,
    int itemCount,
    BigDecimal vendorLineTotal,
    Instant createdAt
) {}

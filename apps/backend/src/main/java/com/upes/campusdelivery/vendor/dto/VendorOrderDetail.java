package com.upes.campusdelivery.vendor.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendorOrderDetail(
    Long orderId,
    String status,
    String zoneName,
    BigDecimal vendorLineTotal,
    Instant createdAt,
    List<VendorOrderItemView> items
) {}

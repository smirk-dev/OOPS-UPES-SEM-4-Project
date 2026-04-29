package com.upes.campusdelivery.vendor.dto;

import java.time.Instant;

public record VendorOrderStatusUpdateResponse(
    Long orderId,
    String previousStatus,
    String newStatus,
    Instant updatedAt
) {}

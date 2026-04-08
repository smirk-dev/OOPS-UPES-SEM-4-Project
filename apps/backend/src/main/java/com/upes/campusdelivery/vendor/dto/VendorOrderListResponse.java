package com.upes.campusdelivery.vendor.dto;

import java.util.List;

public record VendorOrderListResponse(
    List<VendorOrderSummary> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

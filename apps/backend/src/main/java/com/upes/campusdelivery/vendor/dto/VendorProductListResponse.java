package com.upes.campusdelivery.vendor.dto;

import java.util.List;

public record VendorProductListResponse(
    List<VendorProductView> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

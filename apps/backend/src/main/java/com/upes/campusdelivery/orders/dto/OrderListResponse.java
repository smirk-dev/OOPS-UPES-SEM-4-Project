package com.upes.campusdelivery.orders.dto;

import java.util.List;

public record OrderListResponse(
    List<OrderSummaryView> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

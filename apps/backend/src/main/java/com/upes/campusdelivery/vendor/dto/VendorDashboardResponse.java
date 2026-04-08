package com.upes.campusdelivery.vendor.dto;

import java.math.BigDecimal;

public record VendorDashboardResponse(
    Long vendorId,
    String shopName,
    int activeItems,
    int lowStockItems,
    int flashEnabledItems,
    int openOrders,
    BigDecimal recentSalesTotal
) {}

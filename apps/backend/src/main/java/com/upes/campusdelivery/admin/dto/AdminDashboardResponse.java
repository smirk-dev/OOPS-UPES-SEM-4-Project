package com.upes.campusdelivery.admin.dto;

public record AdminDashboardResponse(
    long totalUsers,
    long activeStudents,
    long activeVendors,
    long activeProducts,
    long totalOrders,
    long auditEvents
) {}

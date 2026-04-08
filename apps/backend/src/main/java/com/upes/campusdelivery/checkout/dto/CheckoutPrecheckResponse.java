package com.upes.campusdelivery.checkout.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutPrecheckResponse(
    Long zoneId,
    String zoneName,
    List<CheckoutPrecheckItemView> items,
    BigDecimal subtotal,
    BigDecimal platformDiscount,
    BigDecimal clusterDiscount,
    BigDecimal totalDiscount,
    BigDecimal finalPayable,
    BigDecimal walletBalance,
    boolean walletSufficient,
    boolean clusterEligible,
    String clusterWindowKey
) {}

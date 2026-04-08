package com.upes.campusdelivery.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WalletRechargeRequest(
    @NotNull(message = "Recharge amount is required")
    @DecimalMin(value = "1.00", message = "Recharge amount must be at least 1.00")
    BigDecimal amount,

    String note
) {}

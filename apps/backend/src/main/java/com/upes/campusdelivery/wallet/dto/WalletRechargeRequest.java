package com.upes.campusdelivery.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record WalletRechargeRequest(
    @NotNull(message = "Recharge amount is required")
    @DecimalMin(value = "1.00", message = "Recharge amount must be at least 1.00")
    BigDecimal amount,

    @Size(max = 255, message = "Note must be at most 255 characters")
    String note
) {}

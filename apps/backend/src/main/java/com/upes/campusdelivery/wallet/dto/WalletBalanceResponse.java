package com.upes.campusdelivery.wallet.dto;

import java.math.BigDecimal;

public record WalletBalanceResponse(
    Long walletId,
    Long userId,
    BigDecimal currentBalance
) {}

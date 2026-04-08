package com.upes.campusdelivery.wallet.dto;

import java.math.BigDecimal;

public record WalletRechargeResponse(
    Long walletId,
    Long transactionId,
    BigDecimal creditedAmount,
    BigDecimal updatedBalance
) {}

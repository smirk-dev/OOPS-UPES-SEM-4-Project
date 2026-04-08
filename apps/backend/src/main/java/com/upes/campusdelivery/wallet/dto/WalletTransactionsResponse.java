package com.upes.campusdelivery.wallet.dto;

import java.util.List;

public record WalletTransactionsResponse(
    List<WalletTransactionView> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

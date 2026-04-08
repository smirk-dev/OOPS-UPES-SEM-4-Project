package com.upes.campusdelivery.wallet.controller;

import com.upes.campusdelivery.common.api.ApiResponse;
import com.upes.campusdelivery.wallet.dto.WalletBalanceResponse;
import com.upes.campusdelivery.wallet.dto.WalletRechargeRequest;
import com.upes.campusdelivery.wallet.dto.WalletRechargeResponse;
import com.upes.campusdelivery.wallet.dto.WalletTransactionsResponse;
import com.upes.campusdelivery.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @GetMapping("/balance")
  @PreAuthorize("hasRole('STUDENT')")
  public ApiResponse<WalletBalanceResponse> getBalance(@AuthenticationPrincipal String username) {
    return ApiResponse.ok(walletService.getCurrentUserWalletBalance(username), "n/a");
  }

  @PostMapping("/recharge")
  @PreAuthorize("hasRole('STUDENT')")
  public ApiResponse<WalletRechargeResponse> recharge(
      @AuthenticationPrincipal String username,
      @Valid @RequestBody WalletRechargeRequest request) {
    return ApiResponse.ok(walletService.rechargeCurrentUserWallet(username, request), "n/a");
  }

  @GetMapping("/transactions")
  @PreAuthorize("hasRole('STUDENT')")
  public ApiResponse<WalletTransactionsResponse> getTransactions(
      @AuthenticationPrincipal String username,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.ok(walletService.getCurrentUserTransactions(username, page, size), "n/a");
  }
}

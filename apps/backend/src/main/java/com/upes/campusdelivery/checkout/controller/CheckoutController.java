package com.upes.campusdelivery.checkout.controller;

import com.upes.campusdelivery.checkout.dto.CheckoutPrecheckRequest;
import com.upes.campusdelivery.checkout.dto.CheckoutPrecheckResponse;
import com.upes.campusdelivery.checkout.service.CheckoutService;
import com.upes.campusdelivery.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/precheck")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CheckoutPrecheckResponse>> precheck(
        @AuthenticationPrincipal String username,
        @Valid @RequestBody CheckoutPrecheckRequest request,
        HttpServletRequest httpRequest
    ) {
        CheckoutPrecheckResponse response = checkoutService.precheck(username, request);
        return ResponseEntity.ok(ApiResponse.ok(response, traceId(httpRequest)));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        return traceId == null || traceId.isBlank() ? "n/a" : traceId;
    }
}

package com.upes.campusdelivery.auth.controller;

import com.upes.campusdelivery.auth.dto.LoginRequest;
import com.upes.campusdelivery.auth.dto.LoginResponse;
import com.upes.campusdelivery.auth.service.AuthService;
import com.upes.campusdelivery.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest servletRequest
    ) {
        LoginResponse response = authService.login(request);
        String traceId = servletRequest.getHeader("X-Request-Id");
        return ResponseEntity.ok(ApiResponse.ok(response, traceId == null ? "n/a" : traceId));
    }
}

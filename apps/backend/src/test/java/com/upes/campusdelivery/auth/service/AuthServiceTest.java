package com.upes.campusdelivery.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.upes.campusdelivery.auth.dto.LoginRequest;
import com.upes.campusdelivery.auth.dto.LoginResponse;
import com.upes.campusdelivery.common.enums.Role;
import com.upes.campusdelivery.common.exceptions.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("12345678901234567890123456789012", 3600L);
        authService = new AuthService(new BCryptPasswordEncoder(), jwtService);
    }

    @Test
    void loginReturnsTokenForValidCredentialsAndRole() {
        LoginRequest request = new LoginRequest("student1", "Student@123", Role.STUDENT);
        LoginResponse response = authService.login(request);

        assertEquals(1L, response.userId());
        assertEquals("student1", response.username());
        assertEquals(Role.STUDENT, response.role());
        assertEquals("student1", jwtService.parseClaims(response.token()).getSubject());
        assertEquals("STUDENT", jwtService.parseClaims(response.token()).get("role", String.class));
        assertEquals(3600L, response.expiresInSeconds());
    }

    @Test
    void loginFailsForUnknownUser() {
        LoginRequest request = new LoginRequest("unknown", "Student@123", Role.STUDENT);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void loginFailsForWrongPassword() {
        LoginRequest request = new LoginRequest("student1", "WrongPassword", Role.STUDENT);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void loginFailsForRoleMismatch() {
        LoginRequest request = new LoginRequest("student1", "Student@123", Role.VENDOR);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("ROLE_MISMATCH", exception.getCode());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }
}

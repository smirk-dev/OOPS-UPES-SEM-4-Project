package com.upes.campusdelivery.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.upes.campusdelivery.auth.dto.LoginRequest;
import com.upes.campusdelivery.auth.dto.LoginResponse;
import com.upes.campusdelivery.common.enums.Role;
import com.upes.campusdelivery.common.exceptions.AppException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {

    private JwtService jwtService;
    private AuthService authService;
    private JdbcTemplate jdbcTemplate;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("12345678901234567890123456789012", 3600L);
        authService = new AuthService(jdbcTemplate, passwordEncoder, jwtService);
    }

    @Test
    void loginReturnsTokenForValidCredentialsAndRole() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("student1")))
            .thenReturn(List.of(newUser(1L, "student1", "Student@123", Role.STUDENT, true)));

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
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("unknown"))).thenReturn(List.of());

        LoginRequest request = new LoginRequest("unknown", "Student@123", Role.STUDENT);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void loginFailsForWrongPassword() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("student1")))
            .thenReturn(List.of(newUser(1L, "student1", "Student@123", Role.STUDENT, true)));

        LoginRequest request = new LoginRequest("student1", "WrongPassword", Role.STUDENT);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void loginFailsForRoleMismatch() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("student1")))
            .thenReturn(List.of(newUser(1L, "student1", "Student@123", Role.STUDENT, true)));

        LoginRequest request = new LoginRequest("student1", "Student@123", Role.VENDOR);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("ROLE_MISMATCH", exception.getCode());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void loginFailsForInactiveUser() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("student1")))
            .thenReturn(List.of(newUser(1L, "student1", "Student@123", Role.STUDENT, false)));

        LoginRequest request = new LoginRequest("student1", "Student@123", Role.STUDENT);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals("USER_INACTIVE", exception.getCode());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    private Object newUser(Long id, String username, String rawPassword, Role role, boolean active) {
        try {
            Class<?> userClass = Class.forName("com.upes.campusdelivery.auth.service.AuthService$AuthUser");
            var constructor = userClass.getDeclaredConstructor(Long.class, String.class, String.class, Role.class, boolean.class);
            constructor.setAccessible(true);
            return constructor.newInstance(id, username, passwordEncoder.encode(rawPassword), role, active);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}

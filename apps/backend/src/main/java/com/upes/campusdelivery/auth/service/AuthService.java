package com.upes.campusdelivery.auth.service;

import com.upes.campusdelivery.auth.dto.LoginRequest;
import com.upes.campusdelivery.auth.dto.LoginResponse;
import com.upes.campusdelivery.common.enums.Role;
import com.upes.campusdelivery.common.exceptions.AppException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private record AuthUser(Long id, String username, String encodedPassword, Role role, boolean active) {}

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        AuthUser user = findUserByUsername(request.username());

        if (user == null) {
            throw new AppException("INVALID_CREDENTIALS", "Invalid username or password.", HttpStatus.UNAUTHORIZED);
        }

        if (!user.active()) {
            log.warn("login-failed username={} reason=inactive-user", request.username());
            throw new AppException("USER_INACTIVE", "This account is inactive. Please contact admin.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.password(), user.encodedPassword())) {
            log.warn("login-failed username={} reason=bad-password", request.username());
            throw new AppException("INVALID_CREDENTIALS", "Invalid username or password.", HttpStatus.UNAUTHORIZED);
        }

        if (user.role() != request.role()) {
            log.warn("login-failed username={} reason=role-mismatch requestedRole={} actualRole={}", request.username(), request.role(), user.role());
            throw new AppException("ROLE_MISMATCH", "Selected role does not match this account.", HttpStatus.FORBIDDEN);
        }

        String token = jwtService.generateToken(
            user.username(),
            Map.of("role", user.role().name(), "userId", user.id())
        );

        log.info("login-success username={} role={}", user.username(), user.role());

        return new LoginResponse(user.id(), user.username(), user.role(), token, jwtService.getExpirationSeconds());
    }

    private AuthUser findUserByUsername(String username) {
        List<AuthUser> users = jdbcTemplate.query(
            """
            SELECT id, username, password_hash, role, is_active
            FROM users
            WHERE username = ?
            """,
            (rs, rowNum) -> new AuthUser(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_active")
            ),
            username
        );

        return users.isEmpty() ? null : users.get(0);
    }
}

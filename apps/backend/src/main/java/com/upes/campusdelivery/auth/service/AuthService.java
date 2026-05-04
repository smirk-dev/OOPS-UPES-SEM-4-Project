package com.upes.campusdelivery.auth.service;

import com.upes.campusdelivery.auth.dto.SignupRequest;
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
import org.springframework.transaction.annotation.Transactional;

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
        String username = normalize(request.username());
        AuthUser user = findUserByUsername(username);

        if (user == null) {
            throw new AppException("INVALID_CREDENTIALS", "Invalid username or password.", HttpStatus.UNAUTHORIZED);
        }

        if (!user.active()) {
            log.warn("login-failed username={} reason=inactive-user", request.username());
            throw new AppException("USER_INACTIVE", "This account is inactive. Please contact admin.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.password(), user.encodedPassword())) {
            log.warn("login-failed username={} reason=bad-password", username);
            throw new AppException("INVALID_CREDENTIALS", "Invalid username or password.", HttpStatus.UNAUTHORIZED);
        }

        if (user.role() != request.role()) {
            log.warn("login-failed username={} reason=role-mismatch requestedRole={} actualRole={}", username, request.role(), user.role());
            throw new AppException("ROLE_MISMATCH", "Selected role does not match this account.", HttpStatus.FORBIDDEN);
        }

        String token = buildToken(user.username(), user.role(), user.id());

        log.info("login-success username={} role={}", user.username(), user.role());

        return new LoginResponse(user.id(), user.username(), user.role(), token, jwtService.getExpirationSeconds());
    }

    @Transactional
    public LoginResponse signup(SignupRequest request) {
        String username = normalize(request.username());
        String fullName = normalize(request.fullName());
        String email = normalizeOptional(request.email());
        String phone = normalizeOptional(request.phone());

        if (request.role() == Role.ADMIN) {
            throw new AppException("SIGNUP_NOT_ALLOWED", "Admin accounts are created internally.", HttpStatus.FORBIDDEN);
        }

        if (isBlank(request.password())) {
            throw new AppException("PASSWORD_REQUIRED", "Password is required.", HttpStatus.BAD_REQUEST);
        }

        if (request.role() == Role.VENDOR) {
            if (isBlank(request.shopName())) {
                throw new AppException("SHOP_NAME_REQUIRED", "Shop name is required for vendor signup.", HttpStatus.BAD_REQUEST);
            }
            if (request.vertical() == null) {
                throw new AppException("VERTICAL_REQUIRED", "Vertical is required for vendor signup.", HttpStatus.BAD_REQUEST);
            }
        }

        if (usernameExists(username)) {
            throw new AppException("USERNAME_TAKEN", "Username is already taken.", HttpStatus.CONFLICT);
        }

        if (email != null && emailExists(email)) {
            throw new AppException("EMAIL_TAKEN", "Email is already registered.", HttpStatus.CONFLICT);
        }

        jdbcTemplate.update(
            """
            INSERT INTO users (username, password_hash, role, full_name, email, phone, is_active)
            VALUES (?, ?, ?, ?, ?, ?, TRUE)
            """,
            username,
            passwordEncoder.encode(request.password()),
            request.role().name(),
            fullName,
            email,
            phone
        );

        Long userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE username = ?",
            Long.class,
            username
        );

        if (userId == null) {
            throw new AppException("SIGNUP_FAILED", "Unable to create account.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (request.role() == Role.STUDENT) {
            jdbcTemplate.update(
                "INSERT INTO wallets (user_id, current_balance) VALUES (?, 0)",
                userId
            );
        } else if (request.role() == Role.VENDOR) {
            jdbcTemplate.update(
                "INSERT INTO vendor_profiles (user_id, shop_name, vertical, visibility_state) VALUES (?, ?, ?, 'VISIBLE')",
                userId,
                normalize(request.shopName()),
                request.vertical().name()
            );
        }

        log.info("signup-success username={} role={}", username, request.role());

        return new LoginResponse(
            userId,
            username,
            request.role(),
            buildToken(username, request.role(), userId),
            jwtService.getExpirationSeconds()
        );
    }

    private String buildToken(String username, Role role, Long userId) {
        return jwtService.generateToken(
            username,
            Map.of("role", role.name(), "userId", userId)
        );
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

    private boolean usernameExists(String username) {
        return !jdbcTemplate.queryForList(
            "SELECT 1 FROM users WHERE username = ? LIMIT 1",
            Integer.class,
            username
        ).isEmpty();
    }

    private boolean emailExists(String email) {
        return !jdbcTemplate.queryForList(
            "SELECT 1 FROM users WHERE email = ? LIMIT 1",
            Integer.class,
            email
        ).isEmpty();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

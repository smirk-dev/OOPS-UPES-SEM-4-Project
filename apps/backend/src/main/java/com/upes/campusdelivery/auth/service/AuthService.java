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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private record DemoUser(Long id, String username, String encodedPassword, Role role) {}

    private final List<DemoUser> demoUsers;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        this.demoUsers = List.of(
            new DemoUser(1L, "student1", passwordEncoder.encode("Student@123"), Role.STUDENT),
            new DemoUser(2L, "vendor1", passwordEncoder.encode("Vendor@123"), Role.VENDOR),
            new DemoUser(3L, "admin1", passwordEncoder.encode("Admin@123"), Role.ADMIN)
        );
    }

    public LoginResponse login(LoginRequest request) {
        DemoUser user = demoUsers.stream()
            .filter(item -> item.username().equals(request.username()))
            .findFirst()
            .orElseThrow(() -> new AppException(
                "INVALID_CREDENTIALS",
                "Invalid username or password.",
                HttpStatus.UNAUTHORIZED
            ));

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
}

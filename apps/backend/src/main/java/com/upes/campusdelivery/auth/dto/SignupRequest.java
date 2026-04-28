package com.upes.campusdelivery.auth.dto;

import com.upes.campusdelivery.common.enums.Role;
import com.upes.campusdelivery.common.enums.Vertical;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name is too long")
    String fullName,

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username is too long")
    String username,

    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email is too long")
    String email,

    @Size(max = 20, message = "Phone number is too long")
    String phone,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
    String password,

    @NotNull(message = "Role is required")
    Role role,

    @Size(max = 160, message = "Shop name is too long")
    String shopName,

    Vertical vertical
) {}
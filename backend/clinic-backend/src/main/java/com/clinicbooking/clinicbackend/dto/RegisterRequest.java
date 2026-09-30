package com.clinicbooking.clinicbackend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        String phone,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password,
        @Email String email,
        @AssertTrue(message = "Consent is required to register") boolean consent,
        @NotBlank @Size(min = 6, max = 6, message = "Enter the 6-digit code") String otpCode
) {}
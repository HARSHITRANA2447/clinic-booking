package com.clinicbooking.clinicbackend.dto;

import jakarta.validation.constraints.*;

public record OtpVerifyRequest(
        @Pattern(regexp = "^[6-9]\\d{9}$") String phone,
        @NotBlank @Size(min = 6, max = 6) String code) {}
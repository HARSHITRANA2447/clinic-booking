package com.clinicbooking.clinicbackend.dto;

import jakarta.validation.constraints.Pattern;

public record OtpRequest(
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        String phone) {}
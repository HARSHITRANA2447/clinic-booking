package com.clinicbooking.clinicbackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateDoctorRequest(
        @DecimalMin("0") @DecimalMax("100000") BigDecimal consultationFee,
        @Size(max = 150) String qualification,
        Boolean active) {}
package com.clinicbooking.clinicbackend.dto;

import java.math.BigDecimal;

public record DoctorDetailResponse(
        Long doctorId,
        String doctorName,
        String specialization,
        String qualification,
        BigDecimal consultationFee,
        Long clinicId,
        String clinicName,
        String address,
        String clinicPhone,
        CrowdLevel crowdLevel
) {}
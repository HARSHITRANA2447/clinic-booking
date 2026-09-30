package com.clinicbooking.clinicbackend.dto;

import java.math.BigDecimal;

public record DoctorSearchResponse(
        Long doctorId,
        String doctorName,
        String specialization,
        BigDecimal consultationFee,
        Long clinicId,
        String clinicName,
        String address,
        double distanceKm,
        CrowdLevel crowdLevel
) {}
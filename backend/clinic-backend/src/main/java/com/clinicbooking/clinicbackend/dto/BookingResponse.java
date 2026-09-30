package com.clinicbooking.clinicbackend.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        Long bookingId,
        String status,
        String doctorName,
        String specialization,
        String clinicName,
        String clinicAddress,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
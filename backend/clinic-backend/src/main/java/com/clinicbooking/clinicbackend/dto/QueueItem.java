package com.clinicbooking.clinicbackend.dto;

import java.time.LocalDateTime;

public record QueueItem(Long bookingId, String status, Long slotId,
                        LocalDateTime startTime, LocalDateTime endTime,
                        Long doctorId, String doctorName,
                        String patientName, String patientPhone) {}
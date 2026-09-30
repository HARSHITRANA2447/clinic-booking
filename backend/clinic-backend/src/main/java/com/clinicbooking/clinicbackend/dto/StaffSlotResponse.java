package com.clinicbooking.clinicbackend.dto;

import java.time.LocalDateTime;

public record StaffSlotResponse(Long id, LocalDateTime startTime, LocalDateTime endTime, String status) {}
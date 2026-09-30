package com.clinicbooking.clinicbackend.dto;

import java.time.LocalDateTime;

public record SlotResponse(Long id, LocalDateTime startTime, LocalDateTime endTime) {}
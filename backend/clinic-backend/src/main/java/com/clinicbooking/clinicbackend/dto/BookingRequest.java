package com.clinicbooking.clinicbackend.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(@NotNull Long slotId) {}
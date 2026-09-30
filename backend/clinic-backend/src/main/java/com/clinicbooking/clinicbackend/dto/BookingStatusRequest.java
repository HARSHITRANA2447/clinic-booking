package com.clinicbooking.clinicbackend.dto;

import com.clinicbooking.clinicbackend.entity.Booking;
import jakarta.validation.constraints.NotNull;

public record BookingStatusRequest(@NotNull Booking.Status status) {}
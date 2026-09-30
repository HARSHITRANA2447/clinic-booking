package com.clinicbooking.clinicbackend.dto;

import com.clinicbooking.clinicbackend.entity.Slot;
import jakarta.validation.constraints.NotNull;

public record SlotStatusRequest(@NotNull Slot.Status status) {}
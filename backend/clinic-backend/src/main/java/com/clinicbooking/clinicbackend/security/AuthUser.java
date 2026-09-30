package com.clinicbooking.clinicbackend.security;

import com.clinicbooking.clinicbackend.entity.User;

public record AuthUser(Long userId, Long patientId, Long clinicId, User.Role role) {}
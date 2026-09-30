package com.clinicbooking.clinicbackend.dto;

public record AuthResponse(String token, String role, long expiresInSeconds) {}
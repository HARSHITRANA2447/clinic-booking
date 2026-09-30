package com.clinicbooking.clinicbackend.dto;

public record CrowdResponse(Long clinicId, CrowdLevel level, long booked, long total) {}
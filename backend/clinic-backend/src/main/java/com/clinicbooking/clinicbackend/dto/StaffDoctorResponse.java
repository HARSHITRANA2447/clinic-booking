package com.clinicbooking.clinicbackend.dto;

import java.math.BigDecimal;

public record StaffDoctorResponse(Long id, String name, String specialization,
                                  String qualification, BigDecimal consultationFee,
                                  boolean active) {}
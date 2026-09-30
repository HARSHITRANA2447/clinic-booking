package com.clinicbooking.clinicbackend.repository;
import java.math.BigDecimal;

public interface DoctorSearchRow {
    Long getDoctorId();
    String getDoctorName();
    String getSpecialization();
    BigDecimal getConsultationFee();
    Long getClinicId();
    String getClinicName();
    String getAddress();
    Double getDistanceKm();
}
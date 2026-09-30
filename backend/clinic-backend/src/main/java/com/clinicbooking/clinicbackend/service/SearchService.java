package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.CrowdLevel;
import com.clinicbooking.clinicbackend.dto.DoctorSearchResponse;
import com.clinicbooking.clinicbackend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final DoctorRepository doctorRepository;
    private final CrowdService crowdService;

    @Transactional(readOnly = true)
    public List<DoctorSearchResponse> search(double lat, double lng, double radiusKm, String spec) {
        String specFilter = (spec == null || spec.isBlank()) ? null : spec.trim();

        // Compute crowd once per clinic, since several doctors can share one
        Map<Long, CrowdLevel> crowdByClinic = new HashMap<>();

        return doctorRepository.search(lat, lng, radiusKm, specFilter).stream()
                .map(r -> new DoctorSearchResponse(
                        r.getDoctorId(),
                        r.getDoctorName(),
                        r.getSpecialization(),
                        r.getConsultationFee(),
                        r.getClinicId(),
                        r.getClinicName(),
                        r.getAddress(),
                        Math.round(r.getDistanceKm() * 10.0) / 10.0,
                        crowdByClinic.computeIfAbsent(r.getClinicId(),
                                id -> crowdService.compute(id).level())))
                .toList();
    }
}
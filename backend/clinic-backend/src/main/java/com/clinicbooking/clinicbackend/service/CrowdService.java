package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.CrowdLevel;
import com.clinicbooking.clinicbackend.dto.CrowdResponse;
import com.clinicbooking.clinicbackend.exception.NotFoundException;
import com.clinicbooking.clinicbackend.repository.ClinicRepository;
import com.clinicbooking.clinicbackend.repository.CrowdCounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CrowdService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // Booked-slot ratio thresholds. Tune these once you have real data.
    private static final double MEDIUM_FROM = 0.40;
    private static final double HIGH_FROM = 0.75;

    private final ClinicRepository clinicRepository;

    @Transactional(readOnly = true)
    public CrowdResponse forClinic(Long clinicId) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new NotFoundException("Clinic not found");
        }
        return compute(clinicId);
    }

    // Also called by SearchService, which has already validated the clinic
    @Transactional(readOnly = true)
    public CrowdResponse compute(Long clinicId) {
        LocalDate today = LocalDate.now(IST);
        CrowdCounts counts = clinicRepository.countSlots(
                clinicId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        long total = counts.getTotal() == null ? 0 : counts.getTotal();
        long booked = counts.getBooked() == null ? 0 : counts.getBooked();

        if (total == 0) {
            return new CrowdResponse(clinicId, CrowdLevel.UNKNOWN, 0, 0);
        }
        double ratio = (double) booked / total;
        CrowdLevel level = ratio >= HIGH_FROM ? CrowdLevel.HIGH
                : ratio >= MEDIUM_FROM ? CrowdLevel.MEDIUM
                : CrowdLevel.LOW;
        return new CrowdResponse(clinicId, level, booked, total);
    }
}
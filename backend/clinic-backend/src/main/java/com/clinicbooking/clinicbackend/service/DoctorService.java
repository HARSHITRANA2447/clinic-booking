package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.DoctorDetailResponse;
import com.clinicbooking.clinicbackend.exception.NotFoundException;
import com.clinicbooking.clinicbackend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final CrowdService crowdService;

    @Transactional(readOnly = true)
    public DoctorDetailResponse detail(Long id) {
        var d = doctorRepository.findDetail(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
        var c = d.getClinic();
        return new DoctorDetailResponse(
                d.getId(), d.getName(), d.getSpecialization(), d.getQualification(),
                d.getConsultationFee(), c.getId(), c.getName(), c.getAddress(), c.getPhone(),
                crowdService.compute(c.getId()).level());
    }
}
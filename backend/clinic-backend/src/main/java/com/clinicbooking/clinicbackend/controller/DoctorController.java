package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.DoctorDetailResponse;
import com.clinicbooking.clinicbackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/{id}")
    public DoctorDetailResponse detail(@PathVariable Long id) {
        return doctorService.detail(id);
    }
}
package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.CrowdResponse;
import com.clinicbooking.clinicbackend.service.CrowdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class CrowdController {

    private final CrowdService crowdService;

    @GetMapping("/{clinicId}/crowd")
    public CrowdResponse crowd(@PathVariable Long clinicId) {
        return crowdService.forClinic(clinicId);
    }
}
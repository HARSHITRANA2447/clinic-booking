package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.SlotResponse;
import com.clinicbooking.clinicbackend.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/{doctorId}/slots")
    public List<SlotResponse> slots(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return slotService.available(doctorId, day);
    }
}
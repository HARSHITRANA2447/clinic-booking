package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.*;
import com.clinicbooking.clinicbackend.security.AuthUser;
import com.clinicbooking.clinicbackend.service.ClinicStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/clinic")
@RequiredArgsConstructor
public class ClinicStaffController {

    private final ClinicStaffService service;

    private static LocalDate orToday(LocalDate d) {
        return d != null ? d : LocalDate.now(ZoneId.of("Asia/Kolkata"));
    }

    @GetMapping("/queue")
    public List<QueueItem> queue(@AuthenticationPrincipal AuthUser user,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.queue(user, orToday(date));
    }

    @PatchMapping("/bookings/{id}/status")
    public QueueItem updateBooking(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                   @Valid @RequestBody BookingStatusRequest req) {
        return service.updateBooking(user, id, req.status());
    }

    @GetMapping("/doctors")
    public List<StaffDoctorResponse> doctors(@AuthenticationPrincipal AuthUser user) {
        return service.doctors(user);
    }

    @PatchMapping("/doctors/{id}")
    public StaffDoctorResponse updateDoctor(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                            @Valid @RequestBody UpdateDoctorRequest req) {
        return service.updateDoctor(user, id, req);
    }

    @GetMapping("/doctors/{id}/slots")
    public List<StaffSlotResponse> slots(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.slots(user, id, orToday(date));
    }

    @PostMapping("/doctors/{id}/slots/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateSlotsResponse generate(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                          @Valid @RequestBody GenerateSlotsRequest req) {
        return service.generate(user, id, req);
    }

    @PatchMapping("/slots/{id}/status")
    public StaffSlotResponse setSlotStatus(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                           @Valid @RequestBody SlotStatusRequest req) {
        return service.setSlotStatus(user, id, req.status());
    }
}
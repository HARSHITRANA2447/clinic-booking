package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.BookingRequest;
import com.clinicbooking.clinicbackend.dto.BookingResponse;
import com.clinicbooking.clinicbackend.security.AuthUser;
import com.clinicbooking.clinicbackend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody BookingRequest request,
                                @AuthenticationPrincipal AuthUser user) {
        return bookingService.book(request.slotId(), user.patientId());
    }

    @GetMapping("/mine")
    public List<BookingResponse> mine(@AuthenticationPrincipal AuthUser user) {
        return bookingService.mine(user.patientId());
    }
}
package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.OtpRequest;
import com.clinicbooking.clinicbackend.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@Valid @RequestBody OtpRequest req) {
        otpService.send(req.phone(), "REGISTER");
    }
}
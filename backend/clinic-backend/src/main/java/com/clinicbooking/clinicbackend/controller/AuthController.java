package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.AuthResponse;
import com.clinicbooking.clinicbackend.dto.LoginRequest;
import com.clinicbooking.clinicbackend.dto.RegisterRequest;
import com.clinicbooking.clinicbackend.security.AuthUser;
import com.clinicbooking.clinicbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal AuthUser user) {
        authService.deleteAccount(user.userId(), user.patientId());
    }
}
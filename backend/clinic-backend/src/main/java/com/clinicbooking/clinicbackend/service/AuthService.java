package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.AuthResponse;
import com.clinicbooking.clinicbackend.dto.LoginRequest;
import com.clinicbooking.clinicbackend.dto.RegisterRequest;
import com.clinicbooking.clinicbackend.entity.Patient;
import com.clinicbooking.clinicbackend.entity.User;
import com.clinicbooking.clinicbackend.exception.ConflictException;
import com.clinicbooking.clinicbackend.exception.InvalidCredentialsException;
import com.clinicbooking.clinicbackend.exception.RateLimitException;
import com.clinicbooking.clinicbackend.repository.PatientRepository;
import com.clinicbooking.clinicbackend.repository.UserRepository;
import com.clinicbooking.clinicbackend.security.JwtService;
import com.clinicbooking.clinicbackend.security.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimiterService rateLimiter;
    private final OtpService otpService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (!rateLimiter.tryConsume(
                "register:" + req.phone(),
                5,
                Duration.ofHours(1))) {

            throw new RateLimitException(
                    "Too many attempts. Try again later.");
        }

        otpService.verify(req.phone(), "REGISTER", req.otpCode());

        if (userRepository.existsByPhone(req.phone())
                || patientRepository.findByPhone(req.phone()).isPresent()) {
            throw new ConflictException("This phone number is already registered");
        }

        Patient patient = new Patient();
        patient.setName(req.name());
        patient.setPhone(req.phone());
        patient.setEmail(req.email());
        patient.setConsentAt(LocalDateTime.now(IST));   // DPDP consent captured here
        patient = patientRepository.save(patient);

        User user = new User();
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(User.Role.PATIENT);
        user.setPatientId(patient.getId());
        user = userRepository.save(user);
        user.setPhoneVerified(true);

        return toResponse(user);
    }

    @Transactional
    public void deleteAccount(Long userId, Long patientId) {
        userRepository.deleteById(userId);

        if (patientId != null) {
            // Bookings reference the patient, so anonymize rather than delete,
            // preserving the clinic's records without keeping identifying data.
            patientRepository.findById(patientId).ifPresent(p -> {
                p.setName("Deleted user");
                p.setEmail(null);
                p.setPhone("9" + String.format("%09d", patientId));
            });
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        if (!rateLimiter.tryConsume(
                "login:" + req.phone(),
                8,
                Duration.ofMinutes(15))) {

            throw new RateLimitException(
                    "Too many attempts. Try again in a few minutes.");
        }
        User user = userRepository.findByPhone(req.phone())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();   // same message for both cases
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.generate(user), user.getRole().name(),
                jwtService.expirySeconds());
    }
}
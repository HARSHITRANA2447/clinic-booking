package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.entity.OtpCode;
import com.clinicbooking.clinicbackend.exception.BadRequestException;
import com.clinicbooking.clinicbackend.exception.RateLimitException;
import com.clinicbooking.clinicbackend.repository.OtpCodeRepository;
import com.clinicbooking.clinicbackend.security.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 5;

    private final OtpCodeRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsSender smsSender;
    private final RateLimiterService rateLimiter;

    @Transactional
    public void send(String phone, String purpose) {
        if (!rateLimiter.tryConsume("otp-send:" + phone, 5, Duration.ofHours(1))) {
            throw new RateLimitException("Too many OTP requests. Try again later.");
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpCode entity = new OtpCode();
        entity.setPhone(phone);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setPurpose(purpose);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(entity);

        smsSender.send(phone, "Your ClinicBook verification code is " + code + ". Valid for 10 minutes.");
    }

    /** Throws if invalid; otherwise marks the code consumed. */
    @Transactional
    public void verify(String phone, String purpose, String code) {
        OtpCode entity = otpRepository
                .findFirstByPhoneAndPurposeAndConsumedFalseOrderByIdDesc(phone, purpose)
                .orElseThrow(() -> new BadRequestException("No verification code was requested for this number"));

        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This code has expired. Request a new one.");
        }
        if (entity.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Too many incorrect attempts. Request a new code.");
        }
        if (!passwordEncoder.matches(code, entity.getCodeHash())) {
            entity.setAttempts(entity.getAttempts() + 1);
            throw new BadRequestException("Incorrect code");
        }
        entity.setConsumed(true);
    }
}
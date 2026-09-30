package com.clinicbooking.clinicbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Development stand-in: prints the OTP instead of sending a real SMS.
// Swap this bean for an MSG91/Twilio implementation before real launch.
@Slf4j
@Service
public class ConsoleSmsSender implements SmsSender {
    @Override
    public void send(String phone, String message) {
        log.info("[MOCK SMS to {}]: {}", phone, message);
    }
}
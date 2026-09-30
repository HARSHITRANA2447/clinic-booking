package com.clinicbooking.clinicbackend.service;

public interface SmsSender {
    void send(String phone, String message);
}
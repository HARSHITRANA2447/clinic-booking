package com.clinicbooking.clinicbackend.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Invalid phone or password"); }
}
package com.smartspotsolutions.payment_service.exception;

public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}
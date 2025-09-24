package com.smartspotsolutions.payment_service.exception;

public class VerificationTokenExpiredException extends VerificationException {
    public VerificationTokenExpiredException(String message) {
        super(message);
    }
}
package com.smartspotsolutions.payment_service.exception;

public class InvalidVerificationTokenException extends VerificationException {
    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}
package com.smartspotsolutions.payment_service.io.response;

import com.smartspotsolutions.payment_service.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInitiateResponse {
    private PaymentStatus status; // "SUCCESS", "PENDING", "FAILED"
    private String transactionId;
    private String redirectUrl; // For Paystack, the URL to redirect the user to
    private String message;
}

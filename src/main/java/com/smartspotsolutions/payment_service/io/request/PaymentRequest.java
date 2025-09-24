package com.smartspotsolutions.payment_service.io.request;

import com.smartspotsolutions.payment_service.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private String subscriptionId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String userEmail;
    private String phoneNumber; // Required for Daraja
    private String callbackUrl; // The URL for the webhook to send a response to

    private String transactionId; // Unique transaction ID
    private String description; // Optional description for the payment
}
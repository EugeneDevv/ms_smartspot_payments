package com.smartspotsolutions.payment_service.io;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletionRequest {
    private String transactionId;
    private String subscriptionId;
    private String status; // "SUCCESS", "FAILED"
    private String providerReference; // e.g., Paystack's reference, Daraja's transaction ID
}

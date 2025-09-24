package com.smartspotsolutions.payment_service.io.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompleteRequest {
    private String transactionId;
    private String subscriptionId;
    private String status; // "SUCCESS", "FAILED"
    private String providerReference; // e.g., Paystack's reference, Daraja's transaction ID
}

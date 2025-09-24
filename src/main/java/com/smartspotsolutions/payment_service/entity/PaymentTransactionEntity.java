package com.smartspotsolutions.payment_service.entity;

import com.smartspotsolutions.payment_service.enums.PaymentMethod;
import com.smartspotsolutions.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "smp_payment_transactions")
public class PaymentTransactionEntity extends BaseAuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String transactionId;

    // The subscription ID received from the Main Application
    @Column(nullable = false, unique = true)
    private String subscriptionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private PaymentMethod paymentMethod; // e.g., "BANK", "MPESA"

    @Column(nullable = false)
    private String provider; // e.g., "PAYSTACK", "DARAJA"

    @Column(nullable = false)
    private String transactionReference; // A unique reference for your system

    @Column
    private String providerReference; // The reference from the payment provider

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @PrePersist
    public void generateUuid() {
        if (this.transactionId == null) {
            this.transactionId = UUID.randomUUID().toString();
        }
    }
}

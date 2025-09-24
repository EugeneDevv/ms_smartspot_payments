package com.smartspotsolutions.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "smp_registered_services")
public class RegisteredServiceEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceId;

    @Column(unique = true, nullable = false)
    private String name;  // e.g., "Stock Point"

    private String description;

    // API key (public identifier)
    @Column(unique = true, nullable = false)
    private String apiKey;

    // API secret (hashed, stored securely)
    @Column(nullable = false)
    private String apiSecretHash;

    // Payment details
    private String mpesaShortCode;
    private String currency; // e.g., "KES", "USD"

    @Builder.Default
    private Boolean active = true;

    @PrePersist
    public void generateUuid() {
        if (this.serviceId == null) {
            this.serviceId = UUID.randomUUID().toString();
        }
    }
}


package com.smartspotsolutions.payment_service.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRegistrationResponse {
    private String serviceId;
    private String name;
    private String description;
    private String apiKey;
    private String apiSecret; // plaintext secret, show only once
    private String mpesaShortCode;
    private String currency;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


package com.smartspotsolutions.payment_service.io.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRegisterResponse {
    private String serviceId;
    private String apiKey;
    private String apiSecret; // plaintext secret, show only once
}


package com.smartspotsolutions.payment_service.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegistrationRequest {
    private String name;
    private String description;
    private String mpesaShortCode;
    private String currency;
}


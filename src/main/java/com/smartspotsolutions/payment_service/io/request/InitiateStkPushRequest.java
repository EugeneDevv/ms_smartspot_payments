package com.smartspotsolutions.payment_service.io.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiateStkPushRequest {
    private String amount;
    private String phoneNumber;
}

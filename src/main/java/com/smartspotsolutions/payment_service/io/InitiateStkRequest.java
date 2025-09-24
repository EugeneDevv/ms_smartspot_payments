package com.smartspotsolutions.payment_service.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiateStkRequest {
    private String amount;
    private String phoneNumber;
}

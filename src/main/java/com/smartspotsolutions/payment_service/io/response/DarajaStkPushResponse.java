package com.smartspotsolutions.payment_service.io.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DarajaStkPushResponse {
    private String merchantRequestID;
    private String checkoutRequestID;
    private int responseCode;
    private String responseDescription;
    private String customerMessage;

}

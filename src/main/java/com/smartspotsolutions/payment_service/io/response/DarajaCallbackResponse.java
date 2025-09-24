package com.smartspotsolutions.payment_service.io.response;

import com.smartspotsolutions.payment_service.io.CallbackMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DarajaCallbackResponse {
    private String MerchantRequestID;
    private String CheckoutRequestID;
    private int ResultCode;
    private String ResultDesc;
    private CallbackMetadata CallbackMetadata;
}

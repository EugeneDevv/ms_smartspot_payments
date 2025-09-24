package com.smartspotsolutions.payment_service.io.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DarajaPaymentRequest {
    // The unique M-Pesa business short code.
    @JsonProperty("BusinessShortCode")
    private String businessShortCode;

    // The timestamp of the transaction in the format yyyyMMddHHmmss.
    @JsonProperty("Timestamp")
    private String timestamp;

    // The type of transaction being initiated, e.g., "CustomerPayBillOnline".
    @JsonProperty("TransactionType")
    private String transactionType;

    // The Base64 encoded password for the transaction.
    @JsonProperty("Password")
    private String password;

    // The amount to be paid.
    @JsonProperty("Amount")
    private String amount;

    // The customer's phone number in the format 2547XXXXXXXX.
    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    // The customer's phone number initiating the transaction.
    @JsonProperty("PartyA")
    private String partyA;

    // The M-Pesa business short code.
    @JsonProperty("PartyB")
    private String partyB;

    // The URL where Safaricom will send the transaction result.
    @JsonProperty("CallBackURL")
    private String callBackURL;

    // A brief description of the transaction.
    @JsonProperty("TransactionDesc")
    private String transactionDesc;

    // A unique identifier for the transaction, useful for reconciliation.
    @JsonProperty("AccountReference")
    private String accountReference;
}

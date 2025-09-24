package com.smartspotsolutions.payment_service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspotsolutions.payment_service.io.CallbackMetadata;
import com.smartspotsolutions.payment_service.io.response.DarajaCallbackResponse;
import com.smartspotsolutions.payment_service.io.request.DarajaPaymentRequest;
import com.smartspotsolutions.payment_service.io.response.DarajaStkPushResponse;
import com.smartspotsolutions.payment_service.service.ServiceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class DarajaUtils {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ServiceRegistry serviceRegistry;

    @Value("${daraja.consumer.key}")
    private String consumerKey;

    @Value("${daraja.consumer.secret}")
    private String consumerSecret;

    @Value("${daraja.passkey}")
    private String passKey;

    @Value("${daraja.callback.url}")
    private String callBackUrl;

    @Value("${daraja.shortcode}")
    private String shortCode;

    /**
     * Sends an M-Pesa STK Push request to the Safaricom API.
     * This method correctly uses the DarajaPaymentRequest DTO to build a JSON body.
     *
     * @param amount The amount to be paid.
     * @param phoneNumber The customer's phone number.
     * @return a JsonNode representing the response from the M-Pesa API.
     * @throws JsonProcessingException if there's an issue processing the JSON payload.
     */
    public JsonNode sendStkPush(String amount, String phoneNumber) throws JsonProcessingException {
        // Generate timestamp and password for the STK Push request
        if(serviceRegistry.getAuthenticatedServiceEntity().isPresent()) {
            shortCode = serviceRegistry.getAuthenticatedServiceEntity().get().getMpesaShortCode();
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String passkey = passKey;
        String concatenatedString = shortCode + passkey + timestamp;
        byte[] concatenatedBytes = concatenatedString.getBytes(StandardCharsets.UTF_8);
        String stk_passcode = Base64.getEncoder().encodeToString(concatenatedBytes);


        DarajaPaymentRequest stkRequest = DarajaPaymentRequest.builder()
                .businessShortCode(shortCode)
                .accountReference("SmartSpot")
                .amount(amount)
                .callBackURL(callBackUrl + "/api/v1/payments/callback")
                .partyA(phoneNumber)
                .partyB(shortCode)
                .password(stk_passcode)
                .phoneNumber(phoneNumber)
                .timestamp(timestamp)
                .transactionType("CustomerPayBillOnline")
                .transactionDesc("Testing")
                .build();

        // Convert the DTO object to a JSON string
        String requestBody = objectMapper.writeValueAsString(stkRequest);
        log.info("Request body: {}", requestBody);

        // Authenticate and get the bearer token
        String accessToken = authenticate();
        log.info("Access token: {}", accessToken);

        // Send the STK Push request
        // M-Pesa API URLs
        String PROCESS_REQUEST_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
        JsonNode response = webClient.post()
                .uri(PROCESS_REQUEST_URL)
                .header("Authorization", "Bearer " + accessToken) // Use the correct access token
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        log.info("Response: {}", response);
        assert response != null;
        return response;
    }

    public DarajaStkPushResponse parseDarajaStkPushResponse(JsonNode response) {
        // Parse the STK Push response as needed
        String merchantRequestID = response.path("MerchantRequestID").asText();
        String checkoutRequestID = response.path("CheckoutRequestID").asText();
        int responseCode = response.path("ResponseCode").asInt();
        String responseDescription = response.path("ResponseDescription").asText();
        String customerMessage = response.path("CustomerMessage").asText();

        log.info("STK Push Response - MerchantRequestID: {}, CheckoutRequestID: {}, ResponseCode: {}, ResponseDescription: {}, CustomerMessage: {}",
                merchantRequestID, checkoutRequestID, responseCode, responseDescription, customerMessage);

        return DarajaStkPushResponse.builder()
                .merchantRequestID(merchantRequestID)
                .checkoutRequestID(checkoutRequestID)
                .responseCode(responseCode)
                .responseDescription(responseDescription)
                .customerMessage(customerMessage)
                .build();
    }

    public DarajaCallbackResponse parseDarajaCallback(JsonNode payload) {
        log.info("Received Daraja callback: {}", payload);
        // Process the callback payload as needed
        String resultCode = payload.path("Body").path("stkCallback").path("ResultCode").asText();
        String resultDesc = payload.path("Body").path("stkCallback").path("ResultDesc").asText();
        String merchantRequestID = payload.path("Body").path("stkCallback").path("MerchantRequestID").asText();
        String checkoutRequestID = payload.path("Body").path("stkCallback").path("CheckoutRequestID").asText();
        // Extract callback metadata below
        CallbackMetadata callbackMetadata = null;
        if (payload.path("Body").path("stkCallback").has("CallbackMetadata")) {
            JsonNode callbackMetadataNode = payload.path("Body").path("stkCallback").path("CallbackMetadata");
            try {
                callbackMetadata = objectMapper.treeToValue(callbackMetadataNode, CallbackMetadata.class);
            } catch (JsonProcessingException e) {
                log.error("Error parsing CallbackMetadata: {}", e.getMessage());
            }
        }

        return DarajaCallbackResponse.builder()
                .ResultCode(Integer.parseInt(resultCode))
                .ResultDesc(resultDesc)
                .MerchantRequestID(merchantRequestID)
                .CheckoutRequestID(checkoutRequestID)
                .CallbackMetadata(callbackMetadata)
                .build();
    }

    /**
     * Authenticates with the M-Pesa API to obtain an access token.
     * The method now correctly extracts the access_token from the JSON response body.
     *
     * @return The access token as a string.
     */
    public String authenticate() {
        String basicAuthToken = encodeKeyAndSecret();

        // Make the GET request to the authentication endpoint
        String AUTHENTICATION_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
        JsonNode response = webClient.get()
                .uri(AUTHENTICATION_URL)
                .header("Authorization", "Basic " + basicAuthToken)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        assert response != null;

        // Correctly extract and return the access_token
        return response.get("access_token").asText();
    }

    /**
     * Encodes the consumer key and secret for basic authentication.
     *
     * @return The Base64 encoded string.
     */
    private String encodeKeyAndSecret(){
        String concatenatedString = consumerKey + ":"  + consumerSecret;
        byte[] concatenatedBytes = concatenatedString.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(concatenatedBytes);
    }
}
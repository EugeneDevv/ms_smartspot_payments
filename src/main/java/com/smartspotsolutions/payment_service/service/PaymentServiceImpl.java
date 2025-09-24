package com.smartspotsolutions.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartspotsolutions.payment_service.client.StockPoint;
import com.smartspotsolutions.payment_service.entity.PaymentTransactionEntity;
import com.smartspotsolutions.payment_service.enums.PaymentMethod;
import com.smartspotsolutions.payment_service.enums.PaymentStatus;
import com.smartspotsolutions.payment_service.io.request.PaymentRequest;
import com.smartspotsolutions.payment_service.io.response.DarajaCallbackResponse;
import com.smartspotsolutions.payment_service.io.response.DarajaStkPushResponse;
import com.smartspotsolutions.payment_service.io.response.PaymentInitiateResponse;
import com.smartspotsolutions.payment_service.repository.PaymentTransactionRepository;
import com.smartspotsolutions.payment_service.util.DarajaUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StockPoint mainAppClient;  // Feign client to call the Main Application
    private final DarajaUtils darajaUtils;

    @Transactional
    public PaymentInitiateResponse initiatePayment(PaymentRequest request) throws JsonProcessingException {
        // 1. Create and save a new payment transaction record
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setSubscriptionId(request.getSubscriptionId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());

        // 2. Based on the provider, call the appropriate third-party API
        if (request.getPaymentMethod().equals(PaymentMethod.BANK)) {
            // Logic to call Paystack's API to get a redirect URL
            // Dummy response
            transaction.setProvider("PAYSTACK");
            paymentTransactionRepository.save(transaction);
            return PaymentInitiateResponse.builder()
                    .status(PaymentStatus.PENDING)
                    .transactionId(transaction.getId())
                    .redirectUrl("https://paystack.com/pay/xyz")
                    .build();
        } else if (request.getPaymentMethod().equals(PaymentMethod.MPESA)) {
            // Initiate Push STK
            var response = darajaUtils.sendStkPush(String.valueOf(request.getAmount()), request.getPhoneNumber());
            DarajaStkPushResponse darajaResponse = darajaUtils.parseDarajaStkPushResponse(response);
            if (darajaResponse.getResponseCode() == 0) {
                transaction.setTransactionReference(darajaResponse.getCheckoutRequestID());
                transaction.setProviderReference(darajaResponse.getMerchantRequestID());
                transaction.setStatus(PaymentStatus.PENDING);
            } else {
                transaction.setStatus(PaymentStatus.FAILED);
                paymentTransactionRepository.save(transaction);
                return PaymentInitiateResponse.builder()
                        .status(PaymentStatus.FAILED)
                        .message("Failed to initiate M-Pesa STK Push: " + darajaResponse.getResponseDescription())
                        .build();
            }

            transaction.setProvider("DARAJA");
            paymentTransactionRepository.save(transaction);
            return PaymentInitiateResponse.builder()
                    .status(PaymentStatus.PENDING)
                    .transactionId(transaction.getId())
                    .message("STK Push has been sent to your phone.")
                    .build();
        }
        return PaymentInitiateResponse.builder().status(PaymentStatus.FAILED).message("Invalid payment method").build();
    }

    public void handleDarajaWebhook(DarajaCallbackResponse response) {
        // 1. Extract necessary details from the Daraja callback response
        String checkoutRequestID = response.getCheckoutRequestID();
        String resultCode = String.valueOf(response.getResultCode());
        String resultDesc = response.getResultDesc();

        // 2. Find the payment transaction by checkoutRequestID (you need to store this mapping when initiating payment)
        PaymentTransactionEntity transaction = paymentTransactionRepository.findByTransactionReference(checkoutRequestID)
                .orElseThrow(() -> new RuntimeException("Transaction not found for Daraja callback"));

        // 3. Update the transaction status based on the result code
        if ("0".equals(resultCode)) {
            transaction.setStatus(PaymentStatus.SUCCESS);
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
        }
        paymentTransactionRepository.save(transaction);
//
//        // 4. Call back to the Main Application to complete the subscription
//        PaymentCompleteRequest completionRequest = new PaymentCompleteRequest();
//        completionRequest.setTransactionId(transaction.getId());
//        completionRequest.setSubscriptionId(transaction.getSubscriptionId());
//        completionRequest.setStatus(transaction.getStatus().name());

        mainAppClient.completeSubscription(transaction.getStatus(), transaction.getSubscriptionId());

    }

    private boolean validateSignature(String provider, String payload, String signature) {
        // Here, you would implement the HMAC signature validation logic for each provider.
        // For Paystack, this involves computing a hash of the payload using your secret key.
        // ...
        return true; // Placeholder
    }
    // Helper methods to extract data from webhook payload
    private String extractTransactionReference(String provider, String payload) { /*...*/ return "dummy-ref"; }
    private String extractPaymentStatus(String provider, String payload) { /*...*/ return "success"; }
}
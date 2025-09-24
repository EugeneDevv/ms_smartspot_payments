package com.smartspotsolutions.payment_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartspotsolutions.payment_service.io.DarajaCallbackResponse;
import com.smartspotsolutions.payment_service.io.InitiateStkRequest;
import com.smartspotsolutions.payment_service.io.PaymentInitiationResponse;
import com.smartspotsolutions.payment_service.io.PaymentRequest;
import com.smartspotsolutions.payment_service.service.PaymentServiceImpl;
import com.smartspotsolutions.payment_service.util.DarajaUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    private final PaymentServiceImpl paymentService;
    private final DarajaUtils darajaUtils;

    @PostMapping("/initiate")
    public PaymentInitiationResponse initiatePayment(@RequestBody PaymentRequest request) throws JsonProcessingException {
        log.info("Initiating payment with request: {}", request);
        return paymentService.initiatePayment(request);
    }

    @PostMapping("/send")
    public JsonNode darajaCallBack(@RequestBody @Valid InitiateStkRequest request) {
        try {
            return darajaUtils.sendStkPush(request.getAmount(), request.getPhoneNumber());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/callback")
    public void daraja(@RequestBody JsonNode payload) {
        log.info("Received Daraja callback: {}", payload);
        System.out.println(payload);
        DarajaCallbackResponse response = darajaUtils.parseDarajaCallback(payload);
        paymentService.handleDarajaWebhook(response);
        return;
    }
}

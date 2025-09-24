package com.smartspotsolutions.payment_service.client;

import com.smartspotsolutions.payment_service.enums.PaymentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "stock-point", url = "${stock.point.url}")
public interface StockPoint {
    // This is the endpoint the Payment Service will call back to
    @PostMapping("/subscriptions/internal/complete/{subscriptionId}")
    void completeSubscription(@RequestParam PaymentStatus paymentStatus, @PathVariable String subscriptionId);
}
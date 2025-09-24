package com.smartspotsolutions.payment_service.controller;

import com.smartspotsolutions.payment_service.io.ServiceRegistrationRequest;
import com.smartspotsolutions.payment_service.io.ServiceRegistrationResponse;
import com.smartspotsolutions.payment_service.service.ServiceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceRegistrationController {

    private final ServiceRegistry registry;

    @PostMapping("/register")
    public ResponseEntity<ServiceRegistrationResponse> registerService(@RequestBody ServiceRegistrationRequest request) {
        return ResponseEntity.ok(registry.register(request));
    }

    @PostMapping("/{serviceId}/rotateSecret")
    public ResponseEntity<ServiceRegistrationResponse> rotateSecret(@PathVariable String serviceId) {
        return ResponseEntity.ok(registry.rotateSecret(serviceId));
    }
}


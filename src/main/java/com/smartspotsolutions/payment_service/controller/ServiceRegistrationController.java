package com.smartspotsolutions.payment_service.controller;

import com.smartspotsolutions.payment_service.io.request.ServiceRegisterRequest;
import com.smartspotsolutions.payment_service.io.response.ServiceRegisterResponse;
import com.smartspotsolutions.payment_service.service.ServiceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceRegistrationController {

    private final ServiceRegistry registry;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRegisterResponse registerService(@RequestBody ServiceRegisterRequest request) {
        return registry.register(request);
    }

    @PostMapping("/{serviceId}/rotateSecret")
    public ResponseEntity<ServiceRegisterResponse> rotateSecret(@PathVariable String serviceId) {
        return ResponseEntity.ok(registry.rotateSecret(serviceId));
    }
}


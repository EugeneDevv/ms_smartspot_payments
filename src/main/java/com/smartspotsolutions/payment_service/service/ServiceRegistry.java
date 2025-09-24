package com.smartspotsolutions.payment_service.service;

import com.smartspotsolutions.payment_service.entity.RegisteredServiceEntity;
import com.smartspotsolutions.payment_service.exception.ResourceNotFoundException;
import com.smartspotsolutions.payment_service.io.ServiceRegistrationRequest;
import com.smartspotsolutions.payment_service.io.ServiceRegistrationResponse;
import com.smartspotsolutions.payment_service.repository.RegisteredServiceRepository;
import com.smartspotsolutions.payment_service.util.ApiKeyManager;
import jakarta.servlet.ServletConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRegistry {

    private final RegisteredServiceRepository repository;
    private final ApiKeyManager apiKeyManager;
    private final ServletConfig servletConfig;


    public ServiceRegistrationResponse register(ServiceRegistrationRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Service already registered: " + request.getName());
        }

        String apiKey = generateApiKeyIdentifier(request.getName()); // friendly identifier
        String rawSecret = apiKeyManager.generateRawKey();
        String hashed = apiKeyManager.hashKey(rawSecret);

        RegisteredServiceEntity service = new RegisteredServiceEntity().toBuilder()
                .name(request.getName())
                .description(request.getDescription())
                .apiKey(apiKey)
                .apiSecretHash(hashed)
                .mpesaShortCode(request.getMpesaShortCode())
                .currency(request.getCurrency())
                .build();

        return toResponse(repository.save(service), rawSecret);
    }

    public ServiceRegistrationResponse rotateSecret(String serviceId) {
        RegisteredServiceEntity service = repository.findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        String rawSecret = apiKeyManager.generateRawKey();
        service.setApiSecretHash(apiKeyManager.hashKey(rawSecret));

        return toResponse(repository.save(service), rawSecret);
    }

    public Optional<RegisteredServiceEntity> findByApiKey(String apiKey) {
        return repository.findByApiKey(apiKey);
    }

    private String generateApiKeyIdentifier(String name) {
        // Replace any spaces in the name with an empty string
        String cleanedName = name.replaceAll("\\s", "");

        // Generate a random 8-character string
        String random = apiKeyManager.generateRawKey().substring(0, 32);

        // Combine the cleaned name and random string
        return cleanedName + "-" + random;
    }

    ServiceRegistrationResponse toResponse(RegisteredServiceEntity entity, String secret) {
        return ServiceRegistrationResponse.builder()
                .serviceId(entity.getServiceId())
                .name(entity.getName())
                .description(entity.getDescription())
                .apiKey(entity.getApiKey())
                .apiSecret(secret)
                .mpesaShortCode(entity.getMpesaShortCode())
                .currency(entity.getCurrency())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Optional<RegisteredServiceEntity> getAuthenticatedServiceEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String serviceId = authentication.getName();
        var service = authentication.getDetails();

        return repository.findByServiceId(serviceId);
    }
}


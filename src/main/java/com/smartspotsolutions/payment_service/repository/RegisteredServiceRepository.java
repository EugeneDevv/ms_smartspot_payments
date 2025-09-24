package com.smartspotsolutions.payment_service.repository;

import com.smartspotsolutions.payment_service.entity.RegisteredServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisteredServiceRepository extends JpaRepository<RegisteredServiceEntity, Long> {
    Optional<RegisteredServiceEntity> findByServiceId(String serviceId);
    Optional<RegisteredServiceEntity> findByApiKey(String apiKey);
    Optional<RegisteredServiceEntity> findByName(String name);

    boolean existsByName(String name);
}

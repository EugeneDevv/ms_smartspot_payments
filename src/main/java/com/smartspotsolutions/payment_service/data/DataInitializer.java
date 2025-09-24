package com.smartspotsolutions.payment_service.data;

import com.smartspotsolutions.payment_service.entity.UserEntity;
import com.smartspotsolutions.payment_service.enums.Role;
import com.smartspotsolutions.payment_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        String defaultEmail = "admin@smartspotsolutions.com";
        if (!userRepository.existsByEmail(defaultEmail)) {
            UserEntity user = UserEntity.builder()
                    .email(defaultEmail)
                    .userId(UUID.randomUUID().toString())
                    .firstName("Admin")
                    .lastName("SmartSpot")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(user);
            log.info("Default admin created successfully!");
        }
    }
}
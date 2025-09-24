package com.smartspotsolutions.payment_service.repository;

import com.smartspotsolutions.payment_service.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String defaultEmail);

    Optional<UserEntity> findByUserId(String userId);
    void deleteById(Long id);

    Page<UserEntity> findAll(Specification<UserEntity> spec, Pageable pageable);
}

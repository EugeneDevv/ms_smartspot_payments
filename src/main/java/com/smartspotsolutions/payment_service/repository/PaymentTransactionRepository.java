package com.smartspotsolutions.payment_service.repository;

import com.smartspotsolutions.payment_service.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    Optional<PaymentTransactionEntity> findByTransactionReference(String transactionReference);
}

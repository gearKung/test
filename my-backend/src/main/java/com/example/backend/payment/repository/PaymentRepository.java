package com.example.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.payment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment findByOrderId(String orderId);
}

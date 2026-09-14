package com.cinemahub.cinemahub.order.controller;

import com.cinemahub.cinemahub.order.dto.PaymentResponse;
import com.cinemahub.cinemahub.order.service.PaymentService;
import com.cinemahub.cinemahub.security.access.OwnershipGuard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PaymentController {

    private final PaymentService paymentService;
    private final OwnershipGuard ownershipGuard;

    public PaymentController(PaymentService paymentService, OwnershipGuard ownershipGuard) {
        this.paymentService = paymentService;
        this.ownershipGuard = ownershipGuard;
    }

    @GetMapping("/api/payments/{id}")
    public PaymentResponse findById(@PathVariable Long id) {
        return PaymentResponse.from(paymentService.findById(id));
    }

    @GetMapping("/api/orders/{orderId}/payments")
    public List<PaymentResponse> findByOrder(@PathVariable Long orderId) {
        return paymentService.findByOrder(orderId).stream().map(PaymentResponse::from).toList();
    }

    @GetMapping("/api/users/{userId}/payments")
    public List<PaymentResponse> findByUser(@PathVariable Long userId) {
        ownershipGuard.requireSelfOrAdmin(userId);
        return paymentService.findByUserId(userId).stream().map(PaymentResponse::from).toList();
    }

    @PostMapping("/api/payments/{id}/refund")
    public PaymentResponse refund(@PathVariable Long id) {
        return PaymentResponse.from(paymentService.refund(id));
    }
}
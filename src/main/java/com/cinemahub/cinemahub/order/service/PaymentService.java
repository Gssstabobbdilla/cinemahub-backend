package com.cinemahub.cinemahub.order.service;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.entity.OrderStatus;
import com.cinemahub.cinemahub.order.entity.Payment;
import com.cinemahub.cinemahub.order.entity.PaymentStatus;
import com.cinemahub.cinemahub.order.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * El alta del pago inicial vive en OrderService.registerPayment (necesita coordinar
 * order + reservation en la misma transacción). Este service cubre consultas y el
 * flujo de reembolso, que actúa sobre un pago ya existente.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    public List<Payment> findByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Optional<Payment> findByTransactionCode(String transactionCode) {
        return paymentRepository.findByTransactionCode(transactionCode);
    }

    @Transactional
    public Payment refund(Long paymentId) {
        Payment payment = findById(paymentId);
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new IllegalStateException("Solo un pago APPROVED puede reembolsarse");
        }
        payment.setStatus(PaymentStatus.REFUNDED);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.REFUNDED);

        return payment;
    }
}
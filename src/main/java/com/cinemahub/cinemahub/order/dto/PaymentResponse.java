package com.cinemahub.cinemahub.order.dto;

import com.cinemahub.cinemahub.order.entity.Payment;
import com.cinemahub.cinemahub.order.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String paymentMethod,
        String transactionCode,
        BigDecimal amount,
        PaymentStatus status,
        OffsetDateTime paidAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getOrder().getId(), payment.getPaymentMethod(),
                payment.getTransactionCode(), payment.getAmount(), payment.getStatus(), payment.getPaidAt());
    }
}
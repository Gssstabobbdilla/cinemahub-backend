package com.cinemahub.cinemahub.order.dto;

import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Los productos de la orden no se incluyen acá: se consultan aparte con
// OrderService.findProducts(orderId) -> List<OrderProductResponse>.
public record OrderResponse(
        Long id,
        Long reservationId,
        BigDecimal total,
        OrderStatus status,
        OffsetDateTime purchasedAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getReservation().getId(), order.getTotal(),
                order.getStatus(), order.getPurchasedAt());
    }
}
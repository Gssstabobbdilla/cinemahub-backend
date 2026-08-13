package com.cinemahub.cinemahub.order.dto;

import com.cinemahub.cinemahub.order.entity.OrderProduct;

import java.math.BigDecimal;

public record OrderProductResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    public static OrderProductResponse from(OrderProduct orderProduct) {
        BigDecimal lineTotal = orderProduct.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderProduct.getQuantity()));
        return new OrderProductResponse(
                orderProduct.getProduct().getId(), orderProduct.getProduct().getName(),
                orderProduct.getQuantity(), orderProduct.getUnitPrice(), lineTotal);
    }
}
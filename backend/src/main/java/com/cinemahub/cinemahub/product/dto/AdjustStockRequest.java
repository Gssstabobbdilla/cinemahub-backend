package com.cinemahub.cinemahub.product.dto;

import com.cinemahub.cinemahub.product.entity.MovementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdjustStockRequest(
        @NotNull MovementType movementType,
        @NotNull @Positive Integer quantity
) {
}
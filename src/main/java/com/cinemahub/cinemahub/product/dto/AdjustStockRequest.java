package com.cinemahub.cinemahub.product.dto;

import com.cinemahub.cinemahub.product.entity.MovementType;

public record AdjustStockRequest(MovementType movementType, Integer quantity) {
}
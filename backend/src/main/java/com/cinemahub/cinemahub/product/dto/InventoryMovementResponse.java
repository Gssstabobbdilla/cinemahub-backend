package com.cinemahub.cinemahub.product.dto;

import com.cinemahub.cinemahub.product.entity.InventoryMovement;
import com.cinemahub.cinemahub.product.entity.MovementType;

import java.time.OffsetDateTime;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        MovementType movementType,
        Integer quantity,
        OffsetDateTime createdAt
) {

    public static InventoryMovementResponse from(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(), movement.getProduct().getId(), movement.getMovementType(),
                movement.getQuantity(), movement.getCreatedAt());
    }
}
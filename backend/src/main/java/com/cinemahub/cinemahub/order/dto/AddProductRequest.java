package com.cinemahub.cinemahub.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddProductRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity
) {
}
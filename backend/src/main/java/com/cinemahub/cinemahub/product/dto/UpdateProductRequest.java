package com.cinemahub.cinemahub.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull @PositiveOrZero BigDecimal price,
        @Size(max = 500) String imageUrl,
        @Size(max = 255) String description
) {
}
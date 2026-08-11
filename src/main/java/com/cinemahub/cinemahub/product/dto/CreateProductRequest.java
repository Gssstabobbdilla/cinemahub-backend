package com.cinemahub.cinemahub.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Coincide con ProductService.create(categoryId, name, price). description no se puede
// setear en la creación con el service actual (haría falta un update() adicional).
public record CreateProductRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 150) String name,
        @NotNull @PositiveOrZero BigDecimal price
) {
}
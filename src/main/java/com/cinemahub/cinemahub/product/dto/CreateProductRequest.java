package com.cinemahub.cinemahub.product.dto;

import java.math.BigDecimal;

// Coincide con ProductService.create(categoryId, name, price). description no se puede
// setear en la creación con el service actual (haría falta un update() adicional).
public record CreateProductRequest(Long categoryId, String name, BigDecimal price) {
}
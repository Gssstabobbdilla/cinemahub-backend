package com.cinemahub.cinemahub.product.dto;

import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        ProductStatus status,
        String imageUrl
) {


    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(), product.getCategory().getId(), product.getCategory().getName(),
                product.getName(), product.getDescription(), product.getPrice(),
                product.getStock(), product.getStatus(), product.getImageUrl());
    }
}
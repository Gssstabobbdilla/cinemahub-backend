package com.cinemahub.cinemahub.product.dto;

import com.cinemahub.cinemahub.product.entity.ProductCategory;

public record ProductCategoryResponse(Long id, String name) {

    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(category.getId(), category.getName());
    }
}
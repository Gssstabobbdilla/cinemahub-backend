package com.cinemahub.cinemahub.product.repository;

import com.cinemahub.cinemahub.product.entity.ProductCategory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByName(String name);
}
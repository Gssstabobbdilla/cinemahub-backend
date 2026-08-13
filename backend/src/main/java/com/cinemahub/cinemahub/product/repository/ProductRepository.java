package com.cinemahub.cinemahub.product.repository;

import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStatus(ProductStatus status);
}
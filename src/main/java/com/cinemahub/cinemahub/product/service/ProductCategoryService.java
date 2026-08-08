package com.cinemahub.cinemahub.product.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.repository.ProductCategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAll();
    }

    public ProductCategory findById(Long id) {
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductCategory", id));
    }

    @Transactional
    public ProductCategory create(String name) {
        productCategoryRepository.findByName(name).ifPresent(existing -> {
            throw DuplicateResourceException.of("una categoría", "name", name);
        });
        return productCategoryRepository.save(new ProductCategory(name));
    }

    @Transactional
    public ProductCategory update(Long id, String name) {
        ProductCategory category = findById(id);
        if (!category.getName().equals(name)) {
            productCategoryRepository.findByName(name).ifPresent(existing -> {
                throw DuplicateResourceException.of("una categoría", "name", name);
            });
        }
        category.setName(name);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        productCategoryRepository.delete(findById(id));
    }
}
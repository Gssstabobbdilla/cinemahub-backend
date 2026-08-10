package com.cinemahub.cinemahub.product.controller;

import com.cinemahub.cinemahub.product.dto.ProductCategoryRequest;
import com.cinemahub.cinemahub.product.dto.ProductCategoryResponse;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.service.ProductCategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public List<ProductCategoryResponse> findAll() {
        return productCategoryService.findAll().stream().map(ProductCategoryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductCategoryResponse findById(@PathVariable Long id) {
        return ProductCategoryResponse.from(productCategoryService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryResponse create(@RequestBody ProductCategoryRequest request) {
        ProductCategory category = productCategoryService.create(request.name());
        return ProductCategoryResponse.from(category);
    }

    @PutMapping("/{id}")
    public ProductCategoryResponse update(@PathVariable Long id, @RequestBody ProductCategoryRequest request) {
        ProductCategory category = productCategoryService.update(id, request.name());
        return ProductCategoryResponse.from(category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productCategoryService.delete(id);
    }
}
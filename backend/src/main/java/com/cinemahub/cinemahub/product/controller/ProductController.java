package com.cinemahub.cinemahub.product.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.product.dto.AdjustStockRequest;
import com.cinemahub.cinemahub.product.dto.CreateProductRequest;
import com.cinemahub.cinemahub.product.dto.InventoryMovementResponse;
import com.cinemahub.cinemahub.product.dto.ProductResponse;
import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductStatus;
import com.cinemahub.cinemahub.product.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Requiere categoryId o status: ProductService no expone un findAll() sin filtros.
    @GetMapping
    public List<ProductResponse> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status) {

        List<Product> products;
        if (categoryId != null) {
            products = productService.findByCategory(categoryId);
        } else if (status != null) {
            products = productService.findByStatus(status);
        } else {
            throw new IllegalArgumentException("Se requiere categoryId o status");
        }
        return products.stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return ProductResponse.from(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.create(
            request.categoryId(),
            request.name(),
            request.price(),
            request.imageUrl()
        );
        return ProductResponse.from(product);
    };

    @PostMapping("/{id}/stock")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse adjustStock(@PathVariable Long id, @Valid @RequestBody AdjustStockRequest request) {
        return InventoryMovementResponse.from(
                productService.adjustStock(id, request.movementType(), request.quantity()));
    }

    @GetMapping("/{id}/movements")
    public List<InventoryMovementResponse> findMovements(@PathVariable Long id) {
        return productService.findMovements(id).stream().map(InventoryMovementResponse::from).toList();
    }
}
package com.cinemahub.cinemahub.product.service;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.product.entity.InventoryMovement;
import com.cinemahub.cinemahub.product.entity.MovementType;
import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.entity.ProductStatus;
import com.cinemahub.cinemahub.product.repository.InventoryMovementRepository;
import com.cinemahub.cinemahub.product.repository.ProductCategoryRepository;
import com.cinemahub.cinemahub.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public ProductService(ProductRepository productRepository,
                           ProductCategoryRepository productCategoryRepository,
                           InventoryMovementRepository inventoryMovementRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public List<Product> findByStatus(ProductStatus status) {
        return productRepository.findByStatus(status);
    }

    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    @Transactional
    public Product create(Long categoryId, String name, BigDecimal price) {
        ProductCategory category = productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductCategory", categoryId));
        return productRepository.save(new Product(category, name, price));
    }

    /**
     * Ajusta el stock del producto y deja registro en inventory_movements.
     * IN/ADJUSTMENT suman al stock, OUT resta (y valida que no quede negativo).
     */
    @Transactional
    public InventoryMovement adjustStock(Long productId, MovementType movementType, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        Product product = findById(productId);

        int delta = movementType == MovementType.OUT ? -quantity : quantity;
        int newStock = product.getStock() + delta;
        if (newStock < 0) {
            throw new IllegalStateException("Stock insuficiente para " + product.getName());
        }
        product.setStock(newStock);

        return inventoryMovementRepository.save(new InventoryMovement(product, movementType, quantity));
    }

    public List<InventoryMovement> findMovements(Long productId) {
        return inventoryMovementRepository.findByProductId(productId);
    }
}
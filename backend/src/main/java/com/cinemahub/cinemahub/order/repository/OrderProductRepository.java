package com.cinemahub.cinemahub.order.repository;

import com.cinemahub.cinemahub.order.entity.OrderProduct;
import com.cinemahub.cinemahub.order.entity.OrderProductId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductId> {

    List<OrderProduct> findById_OrderId(Long orderId);

    List<OrderProduct> findById_ProductId(Long productId);
}
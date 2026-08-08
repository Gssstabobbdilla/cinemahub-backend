package com.cinemahub.cinemahub.order.repository;

import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Una reserva genera, como máximo, una orden.
    Optional<Order> findByReservationId(Long reservationId);

    List<Order> findByStatus(OrderStatus status);
}
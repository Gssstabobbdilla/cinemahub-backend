package com.cinemahub.cinemahub.order.controller;

import com.cinemahub.cinemahub.order.dto.AddProductRequest;
import com.cinemahub.cinemahub.order.dto.OrderProductResponse;
import com.cinemahub.cinemahub.order.dto.OrderResponse;
import com.cinemahub.cinemahub.order.dto.PaymentResponse;
import com.cinemahub.cinemahub.order.dto.RegisterPaymentRequest;
import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/reservations/{reservationId}/order")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createFromReservation(@PathVariable Long reservationId) {
        Order order = orderService.createFromReservation(reservationId);
        return OrderResponse.from(order);
    }

    @GetMapping("/api/orders/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return OrderResponse.from(orderService.findById(id));
    }

    @GetMapping("/api/orders/{id}/products")
    public List<OrderProductResponse> findProducts(@PathVariable Long id) {
        return orderService.findProducts(id).stream().map(OrderProductResponse::from).toList();
    }

    @PostMapping("/api/orders/{id}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderProductResponse addProduct(@PathVariable Long id, @RequestBody AddProductRequest request) {
        return OrderProductResponse.from(
                orderService.addProduct(id, request.productId(), request.quantity()));
    }

    @PostMapping("/api/orders/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse registerPayment(@PathVariable Long id, @RequestBody RegisterPaymentRequest request) {
        return PaymentResponse.from(orderService.registerPayment(
                id, request.paymentMethod(), request.transactionCode(), request.approved()));
    }

    @PostMapping("/api/orders/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return OrderResponse.from(orderService.cancel(id));
    }
}
package com.cinemahub.cinemahub.order.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.entity.OrderProduct;
import com.cinemahub.cinemahub.order.entity.OrderStatus;
import com.cinemahub.cinemahub.order.entity.Payment;
import com.cinemahub.cinemahub.order.entity.PaymentStatus;
import com.cinemahub.cinemahub.order.repository.OrderProductRepository;
import com.cinemahub.cinemahub.order.repository.OrderRepository;
import com.cinemahub.cinemahub.order.repository.PaymentRepository;
import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.repository.ProductRepository;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.entity.ReservationSeat;
import com.cinemahub.cinemahub.reservation.repository.ReservationRepository;
import com.cinemahub.cinemahub.reservation.repository.ReservationSeatRepository;
import com.cinemahub.cinemahub.reservation.service.ReservationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationService reservationService;

    public OrderService(OrderRepository orderRepository,
                         ReservationRepository reservationRepository,
                         ReservationSeatRepository reservationSeatRepository,
                         OrderProductRepository orderProductRepository,
                         ProductRepository productRepository,
                         PaymentRepository paymentRepository,
                         ReservationService reservationService) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.orderProductRepository = orderProductRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.reservationService = reservationService;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));
    }

    public Order findByReservation(Long reservationId) {
        return orderRepository.findByReservationId(reservationId)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Order", reservationId));
    }

    /**
     * Crea la orden a partir de una reserva ya existente. El total inicial es la suma
     * de los reservation_seats; addProduct() lo va incrementando si se agregan productos
     * de dulcería antes de pagar.
     */
    @Transactional
    public Order createFromReservation(Long reservationId) {
        if (orderRepository.findByReservationId(reservationId).isPresent()) {
            throw DuplicateResourceException.of("una orden", "reservationId", String.valueOf(reservationId));
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", reservationId));

        BigDecimal seatsTotal = reservationSeatRepository.findByReservationId(reservationId).stream()
                .map(ReservationSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return orderRepository.save(new Order(reservation, seatsTotal));
    }

    @Transactional
    public OrderProduct addProduct(Long orderId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        Order order = findById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden agregar productos a una orden PENDING");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Stock insuficiente para " + product.getName());
        }

        OrderProduct orderProduct = orderProductRepository.save(
                new OrderProduct(order, product, quantity, product.getPrice()));

        product.setStock(product.getStock() - quantity);
        order.setTotal(order.getTotal().add(product.getPrice().multiply(BigDecimal.valueOf(quantity))));

        return orderProduct;
    }

    public List<OrderProduct> findProducts(Long orderId) {
        return orderProductRepository.findById_OrderId(orderId);
    }

    /**
     * Registra el pago y, si fue aprobado, marca la orden como PAID y confirma
     * la reserva asociada (pasa de PENDING a CONFIRMED).
     */
    @Transactional
    public Payment registerPayment(Long orderId, String paymentMethod, String transactionCode, boolean approved) {
        Order order = findById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se puede pagar una orden PENDING");
        }

        Payment payment = new Payment(order, paymentMethod, order.getTotal());
        payment.setTransactionCode(transactionCode);

        if (approved) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setPaidAt(OffsetDateTime.now());
            order.setStatus(OrderStatus.PAID);
            order.setPurchasedAt(OffsetDateTime.now());
            reservationService.confirm(order.getReservation().getId());
        } else {
            payment.setStatus(PaymentStatus.REJECTED);
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("No se puede cancelar una orden ya pagada; usar reembolso");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }

}
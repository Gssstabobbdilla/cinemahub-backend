package com.cinemahub.cinemahub.order.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.membership.entity.Membership;
import com.cinemahub.cinemahub.membership.service.MembershipService;
import com.cinemahub.cinemahub.notification.service.NotificationService;
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
import com.cinemahub.cinemahub.order.entity.OrderProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final NotificationService notificationService;
    private final MembershipService membershipService;

    public OrderService(OrderRepository orderRepository,
                         ReservationRepository reservationRepository,
                         ReservationSeatRepository reservationSeatRepository,
                         OrderProductRepository orderProductRepository,
                         ProductRepository productRepository,
                         PaymentRepository paymentRepository,
                         ReservationService reservationService,
                         NotificationService notificationService,
                         MembershipService membershipService) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.orderProductRepository = orderProductRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.membershipService = membershipService;
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

        // Si el producto ya estaba en la orden, sumamos a la cantidad existente en vez
        // de reemplazarla — evita el bug de que agregar dos veces "resetee" la cantidad
        // a la del último click mientras el total sigue acumulando por separado.
        OrderProductId id = new OrderProductId(orderId, productId);
        OrderProduct orderProduct = orderProductRepository.findById(id).orElse(null);

        if (orderProduct != null) {
            orderProduct.setQuantity(orderProduct.getQuantity() + quantity);
        } else {
            orderProduct = orderProductRepository.save(new OrderProduct(order, product, quantity, product.getPrice()));
        }

        product.setStock(product.getStock() - quantity);
        order.setTotal(order.getTotal().add(product.getPrice().multiply(BigDecimal.valueOf(quantity))));

        return orderProduct;
    }

    public List<OrderProduct> findProducts(Long orderId) {
        return orderProductRepository.findById_OrderId(orderId);
    }

    /**
     * Registra el pago y, si fue aprobado: marca la orden PAID, confirma la reserva
     * (dispara notificación de reserva confirmada), notifica el pago aprobado, y
     * otorga 1 punto de membresía por cada sol gastado — si el usuario no tiene
     * membresía, se omite silenciosamente (es autoservicio, no se crea implícita).
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

            Long userId = order.getReservation().getUser().getId();

            notificationService.create(
                    userId,
                    "Pago aprobado",
                    "Tu pago de S/ %s fue aprobado. Ya podés retirar tus entradas.".formatted(order.getTotal()));

            awardMembershipPoints(userId, order.getTotal());
        } else {
            payment.setStatus(PaymentStatus.REJECTED);
        }

        return paymentRepository.save(payment);
    }

    /**
     * 1 punto por cada sol gastado (redondeado hacia abajo). Si el usuario no tiene
     * membresía todavía, no se le crea una automáticamente — la membresía es
     * autoservicio (ver MembershipService.createForUser / botón en "Mi cuenta").
     */
    private void awardMembershipPoints(Long userId, BigDecimal total) {
        int points = total.setScale(0, RoundingMode.DOWN).intValue();
        if (points <= 0) {
            return;
        }
        try {
            Membership membership = membershipService.findByUser(userId);
            membershipService.adjustPoints(membership.getId(), points, "Compra orden pagada");
        } catch (ResourceNotFoundException ignored) {
            // El usuario no tiene membresía — no se otorgan puntos, no se interrumpe el pago.
        }
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
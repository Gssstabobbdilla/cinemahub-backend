package com.cinemahub.cinemahub;

import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.entity.Seat;
import com.cinemahub.cinemahub.cinema.service.CinemaService;
import com.cinemahub.cinemahub.cinema.service.RoomService;
import com.cinemahub.cinemahub.cinema.service.SeatService;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.service.ClassificationService;
import com.cinemahub.cinemahub.movie.service.MovieService;
import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.entity.OrderStatus;
import com.cinemahub.cinemahub.order.entity.Payment;
import com.cinemahub.cinemahub.order.entity.PaymentStatus;
import com.cinemahub.cinemahub.order.service.OrderService;
import com.cinemahub.cinemahub.product.entity.MovementType;
import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.service.ProductCategoryService;
import com.cinemahub.cinemahub.product.service.ProductService;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.entity.ReservationStatus;
import com.cinemahub.cinemahub.reservation.service.ReservationService;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.repository.UserRepository;
import com.cinemahub.cinemahub.showtime.entity.Showtime;
import com.cinemahub.cinemahub.showtime.service.ShowtimeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo completo de compra: reserva confirmada de asientos -> orden -> agregar producto
 * de dulcería (con descuento de stock) -> pago aprobado -> orden PAID + reserva CONFIRMED.
 */
@SpringBootTest
@Transactional
@Rollback
class OrderFlowTest {

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ClassificationService classificationService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowtimeService showtimeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    private Reservation reservation;
    private Product popcorn;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("Order", "Test", "order.test@cinemahub.local", "hash"));

        Cinema cinema = cinemaService.create("Cine Order Test");
        Room room = roomService.create(cinema.getId(), "Sala 1", 50);
        Seat seat = seatService.create(room.getId(), "B", 5);

        Classification classification = classificationService.create("O-TEST", "test");
        Movie movie = movieService.create("Película Order Test", 100, classification.getId());

        Showtime showtime = showtimeService.create(
                movie.getId(), room.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(18, 0), LocalTime.of(19, 40), new BigDecimal("20.00"));

        reservation = reservationService.createReservation(
                user.getId(), showtime.getId(), List.of(seat.getId()));

        ProductCategory category = productCategoryService.create("Snacks Test");
        popcorn = productService.create(category.getId(), "Canchita", new BigDecimal("12.50"), "https://www.google.com/imgres?q=popcoorn&imgurl=https%3A%2F%2Frecipeforperfection.com%2Fwp-content%2Fuploads%2F2017%2F11%2FMovie-Theater-Popcorn-in-a-popcorn-bucket.jpg&imgrefurl=https%3A%2F%2Frecipeforperfection.com%2Fbetter-than-movie-theater-popcorn%2F&docid=aNODsUZdeIzUsM&tbnid=HkyLet46XU8XQM&vet=12ahUKEwiB6IamnrCWAxWVqpUCHVkSG-YQnPAOegQIMhAA..i&w=680&h=1020&hcb=2&ved=2ahUKEwiB6IamnrCWAxWVqpUCHVkSG-YQnPAOegQIMhAA");
        productService.adjustStock(popcorn.getId(), MovementType.IN, 100);
    }

    @Test
    void createsOrderFromReservationWithSeatsTotal() {
        Order order = orderService.createFromReservation(reservation.getId());

        assertThat(order.getTotal()).isEqualByComparingTo("20.00");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void addingProductIncreasesTotalAndDecreasesStock() {
        Order order = orderService.createFromReservation(reservation.getId());
        int stockBefore = productService.findById(popcorn.getId()).getStock();

        orderService.addProduct(order.getId(), popcorn.getId(), 2);

        assertThat(order.getTotal()).isEqualByComparingTo("45.00"); // 20.00 + 2 * 12.50
        assertThat(productService.findById(popcorn.getId()).getStock()).isEqualTo(stockBefore - 2);
    }

    @Test
    void approvedPaymentMarksOrderPaidAndConfirmsReservation() {
        Order order = orderService.createFromReservation(reservation.getId());

        Payment payment = orderService.registerPayment(order.getId(), "CARD", "TX-TEST-001", true);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(orderService.findById(order.getId()).getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(reservationService.findById(reservation.getId()).getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }
}
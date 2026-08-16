package com.cinemahub.cinemahub.reservation;

import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.entity.Seat;
import com.cinemahub.cinemahub.cinema.service.CinemaService;
import com.cinemahub.cinemahub.cinema.service.RoomService;
import com.cinemahub.cinemahub.cinema.service.SeatService;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.service.ClassificationService;
import com.cinemahub.cinemahub.movie.service.MovieService;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.entity.ReservationStatus;
import com.cinemahub.cinemahub.reservation.service.ReservationService;
import com.cinemahub.cinemahub.reservation.service.SeatAvailability;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * La pieza de lógica más crítica del proyecto: evitar que dos personas reserven el mismo
 * asiento para la misma función. Corre con rollback automático por test.
 */
@SpringBootTest
@Transactional
@Rollback
class ReservationFlowTest {

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
    private UserRepository userRepository;

    private User user;
    private Showtime showtime;
    private Seat seatA1;
    private Seat seatA2;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("Ana", "Test", "ana.reservas@cinemahub.local", "hash"));

        Cinema cinema = cinemaService.create("Cine Reservation Test");
        Room room = roomService.create(cinema.getId(), "Sala 1", 50);
        seatA1 = seatService.create(room.getId(), "A", 1);
        seatA2 = seatService.create(room.getId(), "A", 2);

        Classification classification = classificationService.create("T-TEST", "test");
        Movie movie = movieService.create("Película de prueba", 100, classification.getId());

        showtime = showtimeService.create(
                movie.getId(), room.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(20, 0), LocalTime.of(21, 40), new BigDecimal("25.00"));
    }

    @Test
    void createsReservationWithSelectedSeats() {
        Reservation reservation = reservationService.createReservation(
                user.getId(), showtime.getId(), List.of(seatA1.getId(), seatA2.getId()));

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservationService.findSeats(reservation.getId())).hasSize(2);
    }

    @Test
    void rejectsDoubleBookingOfSameSeat() {
        reservationService.createReservation(user.getId(), showtime.getId(), List.of(seatA1.getId()));

        User otherUser = userRepository.save(
                new User("Beto", "Test", "beto.reservas@cinemahub.local", "hash"));

        assertThatThrownBy(() ->
                reservationService.createReservation(
                        otherUser.getId(), showtime.getId(), List.of(seatA1.getId()))
        ).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void cancelledReservationFreesTheSeat() {
        Reservation first = reservationService.createReservation(
                user.getId(), showtime.getId(), List.of(seatA1.getId()));
        reservationService.cancel(first.getId());

        User otherUser = userRepository.save(
                new User("Caro", "Test", "caro.reservas@cinemahub.local", "hash"));

        Reservation second = reservationService.createReservation(
                otherUser.getId(), showtime.getId(), List.of(seatA1.getId()));

        assertThat(second.getId()).isNotNull();
    }

    @Test
    void confirmReservation() {
        Reservation reservation = reservationService.createReservation(
                user.getId(), showtime.getId(), List.of(seatA1.getId()));

        Reservation confirmed = reservationService.confirm(reservation.getId());

        assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void findSeatAvailabilityMarksOnlyReservedSeatsAsTaken() {
        reservationService.createReservation(user.getId(), showtime.getId(), List.of(seatA1.getId()));

        List<SeatAvailability> availability = reservationService.findSeatAvailability(showtime.getId());

        assertThat(availability).hasSize(2);
        assertThat(availability)
                .filteredOn(a -> a.seat().getId().equals(seatA1.getId()))
                .allMatch(SeatAvailability::taken);
        assertThat(availability)
                .filteredOn(a -> a.seat().getId().equals(seatA2.getId()))
                .allMatch(a -> !a.taken());
    }
}
package com.cinemahub.cinemahub.reservation.service;

import com.cinemahub.cinemahub.cinema.entity.Seat;
import com.cinemahub.cinemahub.cinema.repository.SeatRepository;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.entity.ReservationSeat;
import com.cinemahub.cinemahub.reservation.entity.ReservationStatus;
import com.cinemahub.cinemahub.reservation.repository.ReservationRepository;
import com.cinemahub.cinemahub.reservation.repository.ReservationSeatRepository;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.repository.UserRepository;
import com.cinemahub.cinemahub.showtime.entity.Showtime;
import com.cinemahub.cinemahub.showtime.repository.ShowtimeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final int DEFAULT_HOLD_MINUTES = 10;

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    public ReservationService(ReservationRepository reservationRepository,
                               ReservationSeatRepository reservationSeatRepository,
                               UserRepository userRepository,
                               ShowtimeRepository showtimeRepository,
                               SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.userRepository = userRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", id));
    }

    public List<Reservation> findByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * Crea una reserva temporal (estado PENDING) para uno o más asientos de una función.
     * Valida que ninguno de los asientos ya esté tomado para esa función antes de
     * intentar el insert; la UNIQUE(showtime_id, seat_id) en BD es la red de seguridad
     * final ante condiciones de carrera entre dos solicitudes concurrentes.
     */
    @Transactional
    public Reservation createReservation(Long userId, Long showtimeId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un asiento");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Showtime", showtimeId));

        // Solo cuentan como "tomados" los asientos de reservas todavía vigentes;
        // una reserva CANCELLED o EXPIRED debe liberar el asiento para otros usuarios.
        Set<Long> alreadyTaken = reservationSeatRepository.findByShowtimeId(showtimeId).stream()
                .filter(rs -> rs.getReservation().getStatus() == ReservationStatus.PENDING
                        || rs.getReservation().getStatus() == ReservationStatus.CONFIRMED)
                .map(rs -> rs.getSeat().getId())
                .collect(Collectors.toSet());

        List<Seat> seats = seatIds.stream()
                .map(seatId -> {
                    if (alreadyTaken.contains(seatId)) {
                        throw DuplicateResourceException.of("una reserva", "seatId (función " + showtimeId + ")",
                                String.valueOf(seatId));
                    }
                    return seatRepository.findById(seatId)
                            .orElseThrow(() -> ResourceNotFoundException.of("Seat", seatId));
                })
                .toList();

        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(DEFAULT_HOLD_MINUTES);
        Reservation reservation = reservationRepository.save(new Reservation(user, expiresAt));

        List<ReservationSeat> reservationSeats = seats.stream()
                .map(seat -> new ReservationSeat(reservation, showtime, seat, showtime.getBasePrice()))
                .toList();
        reservationSeatRepository.saveAll(reservationSeats);

        return reservation;
    }

    public List<ReservationSeat> findSeats(Long reservationId) {
        return reservationSeatRepository.findByReservationId(reservationId);
    }

    /**
     * Devuelve TODOS los asientos de la sala de esta función, marcando cuáles ya están
     * tomados. Pensado para que el frontend arme el mapa de butacas antes de reservar.
     * No hace falta filtrar por status de la reserva: cancel()/expireOverdueReservations()
     * ya borran la fila de reservation_seats al liberar un asiento, así que cualquier fila
     * que quede acá para este showtime es, por construcción, de una reserva vigente.
     */
    public List<SeatAvailability> findSeatAvailability(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Showtime", showtimeId));

        Set<Long> takenSeatIds = reservationSeatRepository.findByShowtimeId(showtimeId).stream()
                .map(rs -> rs.getSeat().getId())
                .collect(Collectors.toSet());

        return seatRepository.findByRoomId(showtime.getRoom().getId()).stream()
                .map(seat -> new SeatAvailability(seat, takenSeatIds.contains(seat.getId())))
                .toList();
    }

    @Transactional
    public Reservation confirm(Long reservationId) {
        Reservation reservation = findById(reservationId);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Solo una reserva PENDING puede confirmarse");
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservation;
    }

    /**
     * Cancela la reserva y libera sus asientos borrando las filas de reservation_seats.
     * Es necesario borrarlas (no solo cambiar el status): el UNIQUE(showtime_id, seat_id)
     * es una restricción de fila permanente, así que si la fila queda viva aunque la
     * reserva esté CANCELLED, ese asiento nunca podría volver a reservarse para esa función.
     */
    @Transactional
    public Reservation cancel(Long reservationId) {
        Reservation reservation = findById(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationSeatRepository.deleteByReservationId(reservationId);
        return reservation;
    }

    /**
     * Marca como EXPIRED las reservas PENDING cuyo expires_at ya pasó y libera sus asientos
     * (mismo motivo que en cancel(): hay que borrar las filas de reservation_seats, no solo
     * cambiar el status, porque el UNIQUE(showtime_id, seat_id) bloquearía el asiento para
     * siempre si la fila queda viva).
     * Pensado para invocarse desde un @Scheduled en un componente aparte.
     */
    @Transactional
    public int expireOverdueReservations() {
        List<Reservation> overdue = reservationRepository.findByStatus(ReservationStatus.PENDING).stream()
                .filter(r -> r.getExpiresAt().isBefore(OffsetDateTime.now()))
                .toList();
        overdue.forEach(r -> {
            r.setStatus(ReservationStatus.EXPIRED);
            reservationSeatRepository.deleteByReservationId(r.getId());
        });
        return overdue.size();
    }
}
package com.cinemahub.cinemahub.reservation.controller;

import com.cinemahub.cinemahub.reservation.dto.CreateReservationRequest;
import com.cinemahub.cinemahub.reservation.dto.ReservationResponse;
import com.cinemahub.cinemahub.reservation.dto.ReservationSeatResponse;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.service.ReservationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/api/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.createReservation(
                request.userId(), request.showtimeId(), request.seatIds());
        return ReservationResponse.from(reservation);
    }

    @GetMapping("/api/reservations/{id}")
    public ReservationResponse findById(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.findById(id));
    }

    @GetMapping("/api/reservations/{id}/seats")
    public List<ReservationSeatResponse> findSeats(@PathVariable Long id) {
        return reservationService.findSeats(id).stream().map(ReservationSeatResponse::from).toList();
    }

    @GetMapping("/api/users/{userId}/reservations")
    public List<ReservationResponse> findByUser(@PathVariable Long userId) {
        return reservationService.findByUser(userId).stream().map(ReservationResponse::from).toList();
    }

    @PostMapping("/api/reservations/{id}/confirm")
    public ReservationResponse confirm(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.confirm(id));
    }

    @PostMapping("/api/reservations/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.cancel(id));
    }
}
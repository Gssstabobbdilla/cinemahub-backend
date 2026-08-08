package com.cinemahub.cinemahub.reservation.dto;

import java.util.List;

// userId probablemente debería salir del usuario autenticado (cuando haya Spring Security)
// en vez de venir en el body; por ahora coincide con ReservationService.createReservation.
public record CreateReservationRequest(Long userId, Long showtimeId, List<Long> seatIds) {
}
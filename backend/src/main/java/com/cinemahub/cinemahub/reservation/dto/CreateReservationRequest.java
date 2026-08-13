package com.cinemahub.cinemahub.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// userId probablemente debería salir del usuario autenticado (cuando haya Spring Security)
// en vez de venir en el body; por ahora coincide con ReservationService.createReservation.
public record CreateReservationRequest(
        @NotNull Long userId,
        @NotNull Long showtimeId,
        @NotEmpty List<@NotNull Long> seatIds
) {
}
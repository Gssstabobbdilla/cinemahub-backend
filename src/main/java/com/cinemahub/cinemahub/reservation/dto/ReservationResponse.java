package com.cinemahub.cinemahub.reservation.dto;

import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.entity.ReservationStatus;

import java.time.OffsetDateTime;

// Los asientos no se incluyen acá: se consultan aparte con
// ReservationService.findSeats(reservationId) -> List<ReservationSeatResponse>.
public record ReservationResponse(
        Long id,
        Long userId,
        ReservationStatus status,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(), reservation.getUser().getId(), reservation.getStatus(),
                reservation.getExpiresAt(), reservation.getCreatedAt());
    }
}
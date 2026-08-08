package com.cinemahub.cinemahub.reservation.dto;

import com.cinemahub.cinemahub.reservation.entity.ReservationSeat;

import java.math.BigDecimal;

public record ReservationSeatResponse(Long seatId, String rowLabel, Integer seatNumber, BigDecimal price) {

    public static ReservationSeatResponse from(ReservationSeat reservationSeat) {
        return new ReservationSeatResponse(
                reservationSeat.getSeat().getId(),
                reservationSeat.getSeat().getRowLabel(),
                reservationSeat.getSeat().getSeatNumber(),
                reservationSeat.getPrice());
    }
}
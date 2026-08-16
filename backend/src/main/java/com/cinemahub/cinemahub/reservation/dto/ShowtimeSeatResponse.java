package com.cinemahub.cinemahub.reservation.dto;

import com.cinemahub.cinemahub.reservation.service.SeatAvailability;

public record ShowtimeSeatResponse(
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        String seatType,
        boolean taken
) {

    public static ShowtimeSeatResponse from(SeatAvailability availability) {
        return new ShowtimeSeatResponse(
                availability.seat().getId(),
                availability.seat().getRowLabel(),
                availability.seat().getSeatNumber(),
                availability.seat().getSeatType(),
                availability.taken());
    }
}
package com.cinemahub.cinemahub.cinema.dto;

import com.cinemahub.cinemahub.cinema.entity.Seat;

public record SeatResponse(Long id, Long roomId, String rowLabel, Integer seatNumber, String seatType) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(), seat.getRoom().getId(), seat.getRowLabel(),
                seat.getSeatNumber(), seat.getSeatType());
    }
}
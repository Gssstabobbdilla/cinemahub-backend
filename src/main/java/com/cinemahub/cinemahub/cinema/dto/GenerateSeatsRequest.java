package com.cinemahub.cinemahub.cinema.dto;

import jakarta.validation.constraints.Positive;

// Coincide con SeatService.generateSeatsForRoom(roomId, rowCount, seatsPerRow).
public record GenerateSeatsRequest(
        @Positive int rowCount,
        @Positive int seatsPerRow
) {
}
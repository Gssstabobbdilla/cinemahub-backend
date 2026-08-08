package com.cinemahub.cinemahub.cinema.dto;

// Coincide con SeatService.generateSeatsForRoom(roomId, rowCount, seatsPerRow).
public record GenerateSeatsRequest(int rowCount, int seatsPerRow) {
}
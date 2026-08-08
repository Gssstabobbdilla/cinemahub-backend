package com.cinemahub.cinemahub.cinema.dto;

// El roomId va por path (/rooms/{roomId}/seats), no en el body.
public record CreateSeatRequest(String rowLabel, Integer seatNumber) {
}
package com.cinemahub.cinemahub.reservation.service;

import com.cinemahub.cinemahub.cinema.entity.Seat;

// Projection interna: no es una entidad ni un DTO, es el resultado de combinar "todos los
// asientos de la sala" con "cuáles están tomados para esta función en particular". El
// controller la mapea a ShowtimeSeatResponse antes de devolverla al cliente.
public record SeatAvailability(Seat seat, boolean taken) {
}
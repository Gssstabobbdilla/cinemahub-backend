package com.cinemahub.cinemahub.cinema.dto;

// Coincide con CinemaService.create(name). department/province/district/address se
// completan después con UpdateCinemaLocationRequest (CinemaService.updateLocation).
public record CreateCinemaRequest(String name) {
}
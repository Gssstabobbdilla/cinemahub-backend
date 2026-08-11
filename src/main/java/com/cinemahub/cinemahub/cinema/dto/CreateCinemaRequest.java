package com.cinemahub.cinemahub.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Coincide con CinemaService.create(name). department/province/district/address se
// completan después con UpdateCinemaLocationRequest (CinemaService.updateLocation).
public record CreateCinemaRequest(@NotBlank @Size(max = 150) String name) {
}
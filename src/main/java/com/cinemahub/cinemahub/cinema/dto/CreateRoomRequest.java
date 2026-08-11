package com.cinemahub.cinemahub.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// El cinemaId va por path (/cinemas/{cinemaId}/rooms), no en el body.
public record CreateRoomRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull @Positive Integer capacity
) {
}
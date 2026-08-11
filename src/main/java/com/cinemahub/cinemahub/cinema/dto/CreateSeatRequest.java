package com.cinemahub.cinemahub.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// El roomId va por path (/rooms/{roomId}/seats), no en el body.
public record CreateSeatRequest(
        @NotBlank @Size(max = 5) String rowLabel,
        @NotNull @Positive Integer seatNumber
) {
}
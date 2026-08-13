package com.cinemahub.cinemahub.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Coincide exactamente con MovieService.create(title, duration, classificationId).
// Campos como synopsis, releaseDate, posterUrl o trailerUrl no se pueden setear en la
// creación con el service actual — haría falta un método update() adicional para eso.
public record CreateMovieRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull @Positive Integer duration,
        @NotNull Long classificationId
) {
}
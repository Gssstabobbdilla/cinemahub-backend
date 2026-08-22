package com.cinemahub.cinemahub.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateMovieRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String synopsis,
        @NotNull @Positive Integer duration,
        LocalDate releaseDate,
        @Size(max = 500) String posterUrl,
        @Size(max = 500) String trailerUrl,
        @NotNull Long classificationId
) {
}
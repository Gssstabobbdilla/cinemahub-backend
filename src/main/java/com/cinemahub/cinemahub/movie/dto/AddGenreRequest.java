package com.cinemahub.cinemahub.movie.dto;

import jakarta.validation.constraints.NotNull;

public record AddGenreRequest(@NotNull Long genreId) {
}
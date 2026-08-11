package com.cinemahub.cinemahub.movie.dto;

import com.cinemahub.cinemahub.movie.entity.MovieStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeMovieStatusRequest(@NotNull MovieStatus status) {
}
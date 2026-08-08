package com.cinemahub.cinemahub.movie.dto;

import com.cinemahub.cinemahub.movie.entity.MovieStatus;

public record ChangeMovieStatusRequest(MovieStatus status) {
}
package com.cinemahub.cinemahub.movie.dto;

import com.cinemahub.cinemahub.movie.entity.Genre;

public record GenreResponse(Long id, String name) {

    public static GenreResponse from(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName());
    }
}
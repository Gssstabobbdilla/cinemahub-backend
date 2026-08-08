package com.cinemahub.cinemahub.movie.dto;

import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.entity.MovieStatus;

import java.time.LocalDate;

// Los géneros no se incluyen acá (relación many-to-many vía movie_genres): se consultan
// aparte con MovieService.findGenres(movieId), igual que RoleResponse no incluye permisos.
public record MovieResponse(
        Long id,
        String title,
        String synopsis,
        Integer duration,
        LocalDate releaseDate,
        String posterUrl,
        String trailerUrl,
        ClassificationResponse classification,
        MovieStatus status
) {

    public static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(), movie.getTitle(), movie.getSynopsis(), movie.getDuration(),
                movie.getReleaseDate(), movie.getPosterUrl(), movie.getTrailerUrl(),
                ClassificationResponse.from(movie.getClassification()), movie.getStatus());
    }
}
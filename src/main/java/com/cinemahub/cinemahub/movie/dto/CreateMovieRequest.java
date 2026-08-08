package com.cinemahub.cinemahub.movie.dto;

// Coincide exactamente con MovieService.create(title, duration, classificationId).
// Campos como synopsis, releaseDate, posterUrl o trailerUrl no se pueden setear en la
// creación con el service actual — haría falta un método update() adicional para eso.
public record CreateMovieRequest(String title, Integer duration, Long classificationId) {
}
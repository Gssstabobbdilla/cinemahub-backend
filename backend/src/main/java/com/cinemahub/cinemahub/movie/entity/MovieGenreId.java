package com.cinemahub.cinemahub.movie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MovieGenreId implements Serializable {

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "genre_id")
    private Long genreId;

    protected MovieGenreId() {
        // requerido por JPA
    }

    public MovieGenreId(Long movieId, Long genreId) {
        this.movieId = movieId;
        this.genreId = genreId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getGenreId() {
        return genreId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieGenreId that)) return false;
        return Objects.equals(movieId, that.movieId) && Objects.equals(genreId, that.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, genreId);
    }
}
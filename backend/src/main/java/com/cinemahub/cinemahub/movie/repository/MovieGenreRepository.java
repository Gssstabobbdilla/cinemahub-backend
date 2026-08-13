package com.cinemahub.cinemahub.movie.repository;

import com.cinemahub.cinemahub.movie.entity.MovieGenre;
import com.cinemahub.cinemahub.movie.entity.MovieGenreId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, MovieGenreId> {

    List<MovieGenre> findById_MovieId(Long movieId);

    List<MovieGenre> findById_GenreId(Long genreId);
}
package com.cinemahub.cinemahub.movie.repository;

import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.entity.MovieStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByClassificationId(Long classificationId);
}
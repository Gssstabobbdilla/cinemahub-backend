package com.cinemahub.cinemahub.movie.service;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.entity.Genre;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.entity.MovieGenre;
import com.cinemahub.cinemahub.movie.entity.MovieGenreId;
import com.cinemahub.cinemahub.movie.entity.MovieStatus;
import com.cinemahub.cinemahub.movie.repository.ClassificationRepository;
import com.cinemahub.cinemahub.movie.repository.GenreRepository;
import com.cinemahub.cinemahub.movie.repository.MovieGenreRepository;
import com.cinemahub.cinemahub.movie.repository.MovieRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final ClassificationRepository classificationRepository;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;

    public MovieService(MovieRepository movieRepository,
                         ClassificationRepository classificationRepository,
                         GenreRepository genreRepository,
                         MovieGenreRepository movieGenreRepository) {
        this.movieRepository = movieRepository;
        this.classificationRepository = classificationRepository;
        this.genreRepository = genreRepository;
        this.movieGenreRepository = movieGenreRepository;
    }

    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    public List<Movie> findByStatus(MovieStatus status) {
        return movieRepository.findByStatus(status);
    }

    public Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Movie", id));
    }

    @Transactional
    public Movie create(String title, Integer duration, Long classificationId) {
        Classification classification = classificationRepository.findById(classificationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Classification", classificationId));
        return movieRepository.save(new Movie(title, duration, classification));
    }

    @Transactional
    public Movie changeStatus(Long id, MovieStatus status) {
        Movie movie = findById(id);
        movie.setStatus(status);
        return movie;
    }

    @Transactional
    public MovieGenre addGenre(Long movieId, Long genreId) {
        MovieGenreId id = new MovieGenreId(movieId, genreId);
        return movieGenreRepository.findById(id).orElseGet(() -> {
            Movie movie = findById(movieId);
            Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Genre", genreId));
            return movieGenreRepository.save(new MovieGenre(movie, genre));
        });
    }

    @Transactional
    public void removeGenre(Long movieId, Long genreId) {
        movieGenreRepository.deleteById(new MovieGenreId(movieId, genreId));
    }

    public List<MovieGenre> findGenres(Long movieId) {
        return movieGenreRepository.findById_MovieId(movieId);
    }
}
package com.cinemahub.cinemahub.movie.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.movie.dto.AddGenreRequest;
import com.cinemahub.cinemahub.movie.dto.ChangeMovieStatusRequest;
import com.cinemahub.cinemahub.movie.dto.CreateMovieRequest;
import com.cinemahub.cinemahub.movie.dto.GenreResponse;
import com.cinemahub.cinemahub.movie.dto.MovieResponse;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.entity.MovieStatus;
import com.cinemahub.cinemahub.movie.service.MovieService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<MovieResponse> findAll(@RequestParam(required = false) MovieStatus status) {
        List<Movie> movies = status != null ? movieService.findByStatus(status) : movieService.findAll();
        return movies.stream().map(MovieResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MovieResponse findById(@PathVariable Long id) {
        return MovieResponse.from(movieService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse create(@Valid @RequestBody CreateMovieRequest request) {
        Movie movie = movieService.create(request.title(), request.duration(), request.classificationId());
        return MovieResponse.from(movie);
    }

    @PatchMapping("/{id}/status")
    public MovieResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeMovieStatusRequest request) {
        Movie movie = movieService.changeStatus(id, request.status());
        return MovieResponse.from(movie);
    }

    @GetMapping("/{id}/genres")
    public List<GenreResponse> findGenres(@PathVariable Long id) {
        return movieService.findGenres(id).stream()
                .map(movieGenre -> GenreResponse.from(movieGenre.getGenre()))
                .toList();
    }

    @PostMapping("/{id}/genres")
    @ResponseStatus(HttpStatus.CREATED)
    public void addGenre(@PathVariable Long id, @Valid @RequestBody AddGenreRequest request) {
        movieService.addGenre(id, request.genreId());
    }

    @DeleteMapping("/{id}/genres/{genreId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGenre(@PathVariable Long id, @PathVariable Long genreId) {
        movieService.removeGenre(id, genreId);
    }
}
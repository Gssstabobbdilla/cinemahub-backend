package com.cinemahub.cinemahub.movie.controller;

import com.cinemahub.cinemahub.movie.dto.GenreRequest;
import com.cinemahub.cinemahub.movie.dto.GenreResponse;
import com.cinemahub.cinemahub.movie.entity.Genre;
import com.cinemahub.cinemahub.movie.service.GenreService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<GenreResponse> findAll() {
        return genreService.findAll().stream().map(GenreResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GenreResponse findById(@PathVariable Long id) {
        return GenreResponse.from(genreService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenreResponse create(@RequestBody GenreRequest request) {
        Genre genre = genreService.create(request.name());
        return GenreResponse.from(genre);
    }

    @PutMapping("/{id}")
    public GenreResponse update(@PathVariable Long id, @RequestBody GenreRequest request) {
        Genre genre = genreService.update(id, request.name());
        return GenreResponse.from(genre);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        genreService.delete(id);
    }
}
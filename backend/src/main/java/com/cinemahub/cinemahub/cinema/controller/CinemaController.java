package com.cinemahub.cinemahub.cinema.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.cinema.dto.CinemaResponse;
import com.cinemahub.cinemahub.cinema.dto.CreateCinemaRequest;
import com.cinemahub.cinemahub.cinema.dto.UpdateCinemaLocationRequest;
import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.service.CinemaService;

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
@RequestMapping("/api/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping
    public List<CinemaResponse> findAll() {
        return cinemaService.findAll().stream().map(CinemaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CinemaResponse findById(@PathVariable Long id) {
        return CinemaResponse.from(cinemaService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CinemaResponse create(@Valid @RequestBody CreateCinemaRequest request) {
        Cinema cinema = cinemaService.create(request.name());
        return CinemaResponse.from(cinema);
    }

    @PutMapping("/{id}/location")
    public CinemaResponse updateLocation(@PathVariable Long id, @Valid @RequestBody UpdateCinemaLocationRequest request) {
        Cinema cinema = cinemaService.updateLocation(
                id, request.department(), request.province(), request.district(), request.address());
        return CinemaResponse.from(cinema);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cinemaService.delete(id);
    }
}
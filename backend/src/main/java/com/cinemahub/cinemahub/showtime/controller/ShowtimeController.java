package com.cinemahub.cinemahub.showtime.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.showtime.dto.CreateShowtimeRequest;
import com.cinemahub.cinemahub.showtime.dto.ShowtimeResponse;
import com.cinemahub.cinemahub.showtime.entity.Showtime;
import com.cinemahub.cinemahub.showtime.service.ShowtimeService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    // Requiere al menos movieId, o roomId+date, para no forzar un findAll() sin filtros
    // que el service actual no expone.
    @GetMapping
    public List<ShowtimeResponse> search(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) LocalDate date) {

        List<Showtime> showtimes;
        if (movieId != null) {
            showtimes = showtimeService.findByMovie(movieId);
        } else if (roomId != null && date != null) {
            showtimes = showtimeService.findByRoomAndDate(roomId, date);
        } else {
            throw new IllegalArgumentException("Se requiere movieId, o roomId junto con date");
        }
        return showtimes.stream().map(ShowtimeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ShowtimeResponse findById(@PathVariable Long id) {
        return ShowtimeResponse.from(showtimeService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowtimeResponse create(@Valid @RequestBody CreateShowtimeRequest request) {
        Showtime showtime = showtimeService.create(
                request.movieId(), request.roomId(), request.showDate(),
                request.startTime(), request.endTime(), request.basePrice());
        return ShowtimeResponse.from(showtime);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        showtimeService.cancel(id);
    }
}
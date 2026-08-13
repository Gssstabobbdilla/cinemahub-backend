package com.cinemahub.cinemahub.cinema.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.cinema.dto.CreateSeatRequest;
import com.cinemahub.cinemahub.cinema.dto.GenerateSeatsRequest;
import com.cinemahub.cinemahub.cinema.dto.SeatResponse;
import com.cinemahub.cinemahub.cinema.entity.Seat;
import com.cinemahub.cinemahub.cinema.service.SeatService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/api/rooms/{roomId}/seats")
    public List<SeatResponse> findByRoom(@PathVariable Long roomId) {
        return seatService.findByRoom(roomId).stream().map(SeatResponse::from).toList();
    }

    @PostMapping("/api/rooms/{roomId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatResponse create(@PathVariable Long roomId, @Valid @RequestBody CreateSeatRequest request) {
        Seat seat = seatService.create(roomId, request.rowLabel(), request.seatNumber());
        return SeatResponse.from(seat);
    }

    @PostMapping("/api/rooms/{roomId}/seats/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatResponse> generateSeats(@PathVariable Long roomId, @Valid @RequestBody GenerateSeatsRequest request) {
        return seatService.generateSeatsForRoom(roomId, request.rowCount(), request.seatsPerRow())
                .stream().map(SeatResponse::from).toList();
    }

    @GetMapping("/api/seats/{id}")
    public SeatResponse findById(@PathVariable Long id) {
        return SeatResponse.from(seatService.findById(id));
    }

    @DeleteMapping("/api/seats/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        seatService.delete(id);
    }
}
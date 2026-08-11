package com.cinemahub.cinemahub.cinema.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.cinema.dto.CreateRoomRequest;
import com.cinemahub.cinemahub.cinema.dto.RoomResponse;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.service.RoomService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Sin @RequestMapping a nivel de clase: las rutas mezclan dos recursos padre distintos
// (/cinemas/{cinemaId}/rooms para listar/crear, /rooms/{id} para leer/borrar por id propio).
@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/api/cinemas/{cinemaId}/rooms")
    public List<RoomResponse> findByCinema(@PathVariable Long cinemaId) {
        return roomService.findByCinema(cinemaId).stream().map(RoomResponse::from).toList();
    }

    @PostMapping("/api/cinemas/{cinemaId}/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@PathVariable Long cinemaId, @Valid @RequestBody CreateRoomRequest request) {
        Room room = roomService.create(cinemaId, request.name(), request.capacity());
        return RoomResponse.from(room);
    }

    @GetMapping("/api/rooms/{id}")
    public RoomResponse findById(@PathVariable Long id) {
        return RoomResponse.from(roomService.findById(id));
    }

    @DeleteMapping("/api/rooms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }
}
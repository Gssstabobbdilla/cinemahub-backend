package com.cinemahub.cinemahub.cinema.dto;

import com.cinemahub.cinemahub.cinema.entity.Room;

public record RoomResponse(Long id, Long cinemaId, String name, String type, Integer capacity) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(), room.getCinema().getId(), room.getName(), room.getType(), room.getCapacity());
    }
}
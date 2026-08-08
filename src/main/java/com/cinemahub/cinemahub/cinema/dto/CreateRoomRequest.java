package com.cinemahub.cinemahub.cinema.dto;

// El cinemaId va por path (/cinemas/{cinemaId}/rooms), no en el body.
public record CreateRoomRequest(String name, Integer capacity) {
}
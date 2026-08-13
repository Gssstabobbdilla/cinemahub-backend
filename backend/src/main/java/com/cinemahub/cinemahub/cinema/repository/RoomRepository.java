package com.cinemahub.cinemahub.cinema.repository;

import com.cinemahub.cinemahub.cinema.entity.Room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByCinemaId(Long cinemaId);
    boolean existsByCinemaIdAndName(Long cinemaId, String name);

}
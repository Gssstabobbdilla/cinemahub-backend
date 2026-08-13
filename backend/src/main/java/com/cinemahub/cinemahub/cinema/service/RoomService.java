package com.cinemahub.cinemahub.cinema.service;

import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.repository.CinemaRepository;
import com.cinemahub.cinemahub.cinema.repository.RoomRepository;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;

    public RoomService(RoomRepository roomRepository, CinemaRepository cinemaRepository) {
        this.roomRepository = roomRepository;
        this.cinemaRepository = cinemaRepository;
    }

    public List<Room> findByCinema(Long cinemaId) {
        return roomRepository.findByCinemaId(cinemaId);
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Room", id));
    }

    @Transactional
    public Room create(Long cinemaId, String name, Integer capacity) {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cinema", cinemaId));
        if (roomRepository.existsByCinemaIdAndName(cinemaId, name)) {
            throw DuplicateResourceException.of("una sala", "name", name);
        }
        return roomRepository.save(new Room(cinema, name, capacity));
    }

    @Transactional
    public void delete(Long id) {
        roomRepository.delete(findById(id));
    }
}
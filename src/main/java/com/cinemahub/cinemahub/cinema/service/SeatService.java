package com.cinemahub.cinemahub.cinema.service;

import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.entity.Seat;
import com.cinemahub.cinemahub.cinema.repository.RoomRepository;
import com.cinemahub.cinemahub.cinema.repository.SeatRepository;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;

    public SeatService(SeatRepository seatRepository, RoomRepository roomRepository) {
        this.seatRepository = seatRepository;
        this.roomRepository = roomRepository;
    }

    public List<Seat> findByRoom(Long roomId) {
        return seatRepository.findByRoomId(roomId);
    }

    public Seat findById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Seat", id));
    }

    @Transactional
    public Seat create(Long roomId, String rowLabel, Integer seatNumber) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ResourceNotFoundException.of("Room", roomId));
        if (seatRepository.existsByRoomIdAndRowLabelAndSeatNumber(roomId, rowLabel, seatNumber)) {
            throw DuplicateResourceException.of("una butaca", "posición", rowLabel + seatNumber);
        }
        return seatRepository.save(new Seat(room, rowLabel, seatNumber));
    }

    /**
     * Genera butacas en bloque para una sala: filas de 'A' en adelante,
     * numeradas de 1 a seatsPerRow en cada fila.
     */
    @Transactional
    public List<Seat> generateSeatsForRoom(Long roomId, int rowCount, int seatsPerRow) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ResourceNotFoundException.of("Room", roomId));

        List<Seat> seats = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            String rowLabel = String.valueOf((char) ('A' + rowIndex));
            for (int seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
                seats.add(new Seat(room, rowLabel, seatNumber));
            }
        }
        return seatRepository.saveAll(seats);
    }

    @Transactional
    public void delete(Long id) {
        seatRepository.delete(findById(id));
    }
}
package com.cinemahub.cinemahub.showtime.service;

import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.repository.RoomRepository;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.repository.MovieRepository;
import com.cinemahub.cinemahub.showtime.entity.Showtime;
import com.cinemahub.cinemahub.showtime.entity.ShowtimeStatus;
import com.cinemahub.cinemahub.showtime.repository.ShowtimeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository,
                            MovieRepository movieRepository,
                            RoomRepository roomRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
    }

    public List<Showtime> findByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    public List<Showtime> findByRoomAndDate(Long roomId, LocalDate showDate) {
        return showtimeRepository.findByRoomIdAndShowDate(roomId, showDate);
    }

    public Showtime findById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Showtime", id));
    }

    @Transactional
    public Showtime create(Long movieId, Long roomId, LocalDate showDate,
                            LocalTime startTime, LocalTime endTime, BigDecimal basePrice) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime debe ser posterior a startTime");
        }
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> ResourceNotFoundException.of("Movie", movieId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ResourceNotFoundException.of("Room", roomId));

        // La UNIQUE(room_id, show_date, start_time) en BD es la última línea de defensa;
        // esta validación solo da un mensaje de error más claro antes de llegar ahí.
        boolean duplicate = showtimeRepository.findByRoomIdAndShowDate(roomId, showDate).stream()
                .anyMatch(s -> s.getStartTime().equals(startTime));
        if (duplicate) {
            throw DuplicateResourceException.of("una función", "sala/fecha/hora",
                    roomId + "/" + showDate + "/" + startTime);
        }

        return showtimeRepository.save(new Showtime(movie, room, showDate, startTime, endTime, basePrice));
    }

    @Transactional
    public void cancel(Long id) {
        Showtime showtime = findById(id);
        showtime.setStatus(ShowtimeStatus.CANCELLED);
    }
}
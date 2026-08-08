package com.cinemahub.cinemahub.showtime.repository;

import com.cinemahub.cinemahub.showtime.entity.Showtime;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByRoomIdAndShowDate(Long roomId, LocalDate showDate);
}
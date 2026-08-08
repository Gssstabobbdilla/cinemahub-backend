package com.cinemahub.cinemahub.reservation.repository;

import com.cinemahub.cinemahub.reservation.entity.ReservationSeat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    List<ReservationSeat> findByReservationId(Long reservationId);

    List<ReservationSeat> findByShowtimeId(Long showtimeId);
}
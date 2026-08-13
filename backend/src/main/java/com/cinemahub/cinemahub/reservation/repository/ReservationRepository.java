package com.cinemahub.cinemahub.reservation.repository;

import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.entity.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByStatus(ReservationStatus status);
}
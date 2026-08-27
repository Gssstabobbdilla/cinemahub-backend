package com.cinemahub.cinemahub.reservation.scheduler;

import com.cinemahub.cinemahub.reservation.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Corre cada minuto: libera asientos de reservas PENDING cuyo hold de 10 minutos
    // ya venció, para que otros usuarios puedan volver a reservarlos.
    @Scheduled(fixedRate = 60_000)
    public void expireOverdueReservations() {
        int expired = reservationService.expireOverdueReservations();
        if (expired > 0) {
            log.info("Se expiraron {} reserva(s) vencidas", expired);
        }
    }
}
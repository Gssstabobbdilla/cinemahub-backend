package com.cinemahub.cinemahub.showtime.dto;

import com.cinemahub.cinemahub.showtime.entity.Showtime;
import com.cinemahub.cinemahub.showtime.entity.ShowtimeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Incluye movieTitle/roomName/cinemaName además de los ids: en una cartelera el cliente
// casi siempre necesita mostrar esos nombres, y evita que tenga que pedirlos aparte
// para cada función (a diferencia de Role/Permission, acá el N+1 sí sería un problema real).
public record ShowtimeResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long roomId,
        String roomName,
        String cinemaName,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        String language,
        String format,
        BigDecimal basePrice,
        ShowtimeStatus status
) {

    public static ShowtimeResponse from(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getMovie().getId(), showtime.getMovie().getTitle(),
                showtime.getRoom().getId(), showtime.getRoom().getName(),
                showtime.getRoom().getCinema().getName(),
                showtime.getShowDate(), showtime.getStartTime(), showtime.getEndTime(),
                showtime.getLanguage(), showtime.getFormat(), showtime.getBasePrice(),
                showtime.getStatus());
    }
}
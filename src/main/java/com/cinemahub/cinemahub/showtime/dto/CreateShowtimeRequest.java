package com.cinemahub.cinemahub.showtime.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateShowtimeRequest(
        Long movieId,
        Long roomId,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal basePrice
) {
}
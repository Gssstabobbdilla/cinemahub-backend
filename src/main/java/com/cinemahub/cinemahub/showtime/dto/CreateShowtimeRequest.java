package com.cinemahub.cinemahub.showtime.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// El orden startTime < endTime ya lo valida ShowtimeService.create (IllegalArgumentException),
// no se duplica acá para no repetir la misma regla en dos capas.
public record CreateShowtimeRequest(
        @NotNull Long movieId,
        @NotNull Long roomId,
        @NotNull @FutureOrPresent LocalDate showDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull @PositiveOrZero BigDecimal basePrice
) {
}
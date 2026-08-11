package com.cinemahub.cinemahub.promotion.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// Coincide con PromotionService.create(title, discountPercentage, startDate, endDate).
// description no se puede setear en la creación con el service actual. El orden
// endDate >= startDate ya lo garantiza el CHECK de la BD; si querés que falle antes
// (400 en vez de 500 por violación de constraint), se puede agregar una validación
// a nivel de clase — avisame si la querés.
public record CreatePromotionRequest(
        @NotBlank @Size(max = 150) String title,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal discountPercentage,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
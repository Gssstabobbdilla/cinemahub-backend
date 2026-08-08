package com.cinemahub.cinemahub.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Coincide con PromotionService.create(title, discountPercentage, startDate, endDate).
// description no se puede setear en la creación con el service actual.
public record CreatePromotionRequest(
        String title,
        BigDecimal discountPercentage,
        LocalDate startDate,
        LocalDate endDate
) {
}
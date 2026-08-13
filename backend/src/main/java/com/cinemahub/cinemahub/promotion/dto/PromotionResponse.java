package com.cinemahub.cinemahub.promotion.dto;

import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.entity.PromotionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionResponse(
        Long id,
        String title,
        String description,
        BigDecimal discountPercentage,
        LocalDate startDate,
        LocalDate endDate,
        PromotionStatus status
) {

    public static PromotionResponse from(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(), promotion.getTitle(), promotion.getDescription(),
                promotion.getDiscountPercentage(), promotion.getStartDate(), promotion.getEndDate(),
                promotion.getStatus());
    }
}
package com.cinemahub.cinemahub.promotion.dto;

import com.cinemahub.cinemahub.promotion.entity.PromotionStatus;

import jakarta.validation.constraints.NotNull;

public record ChangePromotionStatusRequest(@NotNull PromotionStatus status) {
}
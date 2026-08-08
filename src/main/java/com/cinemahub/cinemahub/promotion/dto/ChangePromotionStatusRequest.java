package com.cinemahub.cinemahub.promotion.dto;

import com.cinemahub.cinemahub.promotion.entity.PromotionStatus;

public record ChangePromotionStatusRequest(PromotionStatus status) {
}
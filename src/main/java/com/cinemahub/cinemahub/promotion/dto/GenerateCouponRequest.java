package com.cinemahub.cinemahub.promotion.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// El promotionId va por path (/promotions/{promotionId}/coupons), no en el body.
public record GenerateCouponRequest(String code, BigDecimal discountPercentage, OffsetDateTime expiresAt) {
}
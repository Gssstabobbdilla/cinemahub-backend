package com.cinemahub.cinemahub.promotion.dto;

import com.cinemahub.cinemahub.promotion.entity.Coupon;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CouponResponse(
        Long id,
        Long promotionId,
        String code,
        BigDecimal discountPercentage,
        OffsetDateTime expiresAt
) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(), coupon.getPromotion().getId(), coupon.getCode(),
                coupon.getDiscountPercentage(), coupon.getExpiresAt());
    }
}
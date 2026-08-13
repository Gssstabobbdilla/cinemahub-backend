package com.cinemahub.cinemahub.promotion.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// El promotionId va por path (/promotions/{promotionId}/coupons), no en el body.
public record GenerateCouponRequest(
        @NotBlank @Size(max = 50) String code,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal discountPercentage,
        @NotNull @Future OffsetDateTime expiresAt
) {
}
package com.cinemahub.cinemahub.promotion.controller;

import com.cinemahub.cinemahub.promotion.dto.CouponResponse;
import com.cinemahub.cinemahub.promotion.dto.GenerateCouponRequest;
import com.cinemahub.cinemahub.promotion.entity.Coupon;
import com.cinemahub.cinemahub.promotion.service.CouponService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/api/promotions/{promotionId}/coupons")
    public List<CouponResponse> findByPromotion(@PathVariable Long promotionId) {
        return couponService.findByPromotion(promotionId).stream().map(CouponResponse::from).toList();
    }

    @PostMapping("/api/promotions/{promotionId}/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse generate(@PathVariable Long promotionId, @RequestBody GenerateCouponRequest request) {
        Coupon coupon = couponService.generate(
                promotionId, request.code(), request.discountPercentage(), request.expiresAt());
        return CouponResponse.from(coupon);
    }

    // Valida que el cupón exista y no esté vencido (CouponService.validate lanza si no).
    @GetMapping("/api/coupons/{code}/validate")
    public CouponResponse validate(@PathVariable String code) {
        return CouponResponse.from(couponService.validate(code));
    }
}
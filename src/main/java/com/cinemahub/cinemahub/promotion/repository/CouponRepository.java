package com.cinemahub.cinemahub.promotion.repository;

import com.cinemahub.cinemahub.promotion.entity.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByPromotionId(Long promotionId);
}
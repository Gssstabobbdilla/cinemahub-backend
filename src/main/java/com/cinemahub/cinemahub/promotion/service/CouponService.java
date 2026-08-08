package com.cinemahub.cinemahub.promotion.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.promotion.entity.Coupon;
import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.repository.CouponRepository;
import com.cinemahub.cinemahub.promotion.repository.PromotionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;

    public CouponService(CouponRepository couponRepository, PromotionRepository promotionRepository) {
        this.couponRepository = couponRepository;
        this.promotionRepository = promotionRepository;
    }

    public List<Coupon> findByPromotion(Long promotionId) {
        return couponRepository.findByPromotionId(promotionId);
    }

    @Transactional
    public Coupon generate(Long promotionId, String code, BigDecimal discountPercentage, OffsetDateTime expiresAt) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Promotion", promotionId));
        couponRepository.findByCode(code).ifPresent(existing -> {
            throw DuplicateResourceException.of("un cupón", "code", code);
        });
        return couponRepository.save(new Coupon(promotion, code, discountPercentage, expiresAt));
    }

    /**
     * Valida que el cupón exista y no haya expirado. Lanza excepción si no es válido;
     * quien llame decide qué hacer con el descuento (aplicarlo al total de una orden, etc.).
     */
    public Coupon validate(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + code));
        if (coupon.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("El cupón " + code + " ya expiró");
        }
        return coupon;
    }
}
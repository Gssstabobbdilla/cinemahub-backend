package com.cinemahub.cinemahub.promotion.repository;

import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.entity.PromotionStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByStatus(PromotionStatus status);
}
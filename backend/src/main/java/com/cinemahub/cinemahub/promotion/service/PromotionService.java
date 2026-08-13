package com.cinemahub.cinemahub.promotion.service;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.entity.PromotionStatus;
import com.cinemahub.cinemahub.promotion.repository.PromotionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<Promotion> findByStatus(PromotionStatus status) {
        return promotionRepository.findByStatus(status);
    }

    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Promotion", id));
    }

    @Transactional
    public Promotion create(String title, BigDecimal discountPercentage, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate no puede ser anterior a startDate");
        }
        return promotionRepository.save(new Promotion(title, discountPercentage, startDate, endDate));
    }

    @Transactional
    public Promotion changeStatus(Long id, PromotionStatus status) {
        Promotion promotion = findById(id);
        promotion.setStatus(status);
        return promotion;
    }
}
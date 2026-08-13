package com.cinemahub.cinemahub.promotion.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.promotion.dto.ChangePromotionStatusRequest;
import com.cinemahub.cinemahub.promotion.dto.CreatePromotionRequest;
import com.cinemahub.cinemahub.promotion.dto.PromotionResponse;
import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.entity.PromotionStatus;
import com.cinemahub.cinemahub.promotion.service.PromotionService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // PromotionService solo expone findByStatus; por defecto se listan las ACTIVE.
    @GetMapping
    public List<PromotionResponse> findByStatus(
            @RequestParam(required = false, defaultValue = "ACTIVE") PromotionStatus status) {
        return promotionService.findByStatus(status).stream().map(PromotionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PromotionResponse findById(@PathVariable Long id) {
        return PromotionResponse.from(promotionService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create(@Valid @RequestBody CreatePromotionRequest request) {
        Promotion promotion = promotionService.create(
                request.title(), request.discountPercentage(), request.startDate(), request.endDate());
        return PromotionResponse.from(promotion);
    }

    @PatchMapping("/{id}/status")
    public PromotionResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangePromotionStatusRequest request) {
        Promotion promotion = promotionService.changeStatus(id, request.status());
        return PromotionResponse.from(promotion);
    }
}
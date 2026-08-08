package com.cinemahub.cinemahub.movie.dto;

import com.cinemahub.cinemahub.movie.entity.Classification;

public record ClassificationResponse(Long id, String code, String description) {

    public static ClassificationResponse from(Classification classification) {
        return new ClassificationResponse(
                classification.getId(), classification.getCode(), classification.getDescription());
    }
}
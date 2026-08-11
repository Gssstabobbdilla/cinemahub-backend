package com.cinemahub.cinemahub.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassificationRequest(
        @NotBlank @Size(max = 10) String code,
        @Size(max = 255) String description
) {
}
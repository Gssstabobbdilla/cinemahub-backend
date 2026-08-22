package com.cinemahub.cinemahub.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCinemaRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 100) String department,
        @Size(max = 100) String province,
        @Size(max = 100) String district,
        @Size(max = 255) String address
) {
}
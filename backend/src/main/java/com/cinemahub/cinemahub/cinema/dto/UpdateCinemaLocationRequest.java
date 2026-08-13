package com.cinemahub.cinemahub.cinema.dto;

import jakarta.validation.constraints.Size;

public record UpdateCinemaLocationRequest(
        @Size(max = 100) String department,
        @Size(max = 100) String province,
        @Size(max = 100) String district,
        @Size(max = 255) String address
) {
}
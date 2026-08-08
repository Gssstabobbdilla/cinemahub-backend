package com.cinemahub.cinemahub.cinema.dto;

import com.cinemahub.cinemahub.cinema.entity.Cinema;

import java.math.BigDecimal;

public record CinemaResponse(
        Long id,
        String name,
        String department,
        String province,
        String district,
        String address,
        String phone,
        BigDecimal latitude,
        BigDecimal longitude
) {

    public static CinemaResponse from(Cinema cinema) {
        return new CinemaResponse(
                cinema.getId(), cinema.getName(), cinema.getDepartment(), cinema.getProvince(),
                cinema.getDistrict(), cinema.getAddress(), cinema.getPhone(),
                cinema.getLatitude(), cinema.getLongitude());
    }
}
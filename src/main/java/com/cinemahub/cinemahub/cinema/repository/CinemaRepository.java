package com.cinemahub.cinemahub.cinema.repository;

import com.cinemahub.cinemahub.cinema.entity.Cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findByDepartmentAndProvinceAndDistrict(String department, String province, String district);
}
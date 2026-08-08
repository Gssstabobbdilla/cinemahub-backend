package com.cinemahub.cinemahub.cinema.service;

import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.repository.CinemaRepository;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CinemaService {

    private final CinemaRepository cinemaRepository;

    public CinemaService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    public List<Cinema> findAll() {
        return cinemaRepository.findAll();
    }

    public Cinema findById(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Cinema", id));
    }

    @Transactional
    public Cinema create(String name) {
        return cinemaRepository.save(new Cinema(name));
    }

    @Transactional
    public Cinema updateLocation(Long id, String department, String province, String district, String address) {
        Cinema cinema = findById(id);
        cinema.setDepartment(department);
        cinema.setProvince(province);
        cinema.setDistrict(district);
        cinema.setAddress(address);
        return cinema;
    }

    @Transactional
    public void delete(Long id) {
        cinemaRepository.delete(findById(id));
    }
}
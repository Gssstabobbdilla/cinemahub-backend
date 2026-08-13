package com.cinemahub.cinemahub.movie.repository;

import com.cinemahub.cinemahub.movie.entity.Classification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassificationRepository extends JpaRepository<Classification, Long> {

    Optional<Classification> findByCode(String code);
}
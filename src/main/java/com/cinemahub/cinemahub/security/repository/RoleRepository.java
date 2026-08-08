package com.cinemahub.cinemahub.security.repository;

import com.cinemahub.cinemahub.security.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
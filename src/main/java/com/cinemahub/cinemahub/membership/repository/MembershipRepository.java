package com.cinemahub.cinemahub.membership.repository;

import com.cinemahub.cinemahub.membership.entity.Membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    // Un usuario tiene, como máximo, una membresía.
    Optional<Membership> findByUserId(Long userId);
}
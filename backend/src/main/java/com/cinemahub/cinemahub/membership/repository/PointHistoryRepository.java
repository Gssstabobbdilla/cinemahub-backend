package com.cinemahub.cinemahub.membership.repository;

import com.cinemahub.cinemahub.membership.entity.PointHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByMembershipId(Long membershipId);
}
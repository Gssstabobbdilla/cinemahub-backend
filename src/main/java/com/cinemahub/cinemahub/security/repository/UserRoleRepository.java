package com.cinemahub.cinemahub.security.repository;

import com.cinemahub.cinemahub.security.entity.UserRole;
import com.cinemahub.cinemahub.security.entity.UserRoleId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findById_UserId(Long userId);

    List<UserRole> findById_RoleId(Long roleId);
}
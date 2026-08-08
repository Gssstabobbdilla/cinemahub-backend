package com.cinemahub.cinemahub.security.repository;

import com.cinemahub.cinemahub.security.entity.RolePermission;
import com.cinemahub.cinemahub.security.entity.RolePermissionId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findById_RoleId(Long roleId);

    List<RolePermission> findById_PermissionId(Long permissionId);
}
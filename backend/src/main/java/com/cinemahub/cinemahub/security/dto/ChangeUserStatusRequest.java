package com.cinemahub.cinemahub.security.dto;

import com.cinemahub.cinemahub.security.entity.UserStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeUserStatusRequest(@NotNull UserStatus status) {
}
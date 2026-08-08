package com.cinemahub.cinemahub.security.dto;

import com.cinemahub.cinemahub.security.entity.UserStatus;

public record ChangeUserStatusRequest(UserStatus status) {
}
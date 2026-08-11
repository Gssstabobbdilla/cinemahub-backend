package com.cinemahub.cinemahub.membership.dto;

import com.cinemahub.cinemahub.membership.entity.MembershipLevel;

import jakarta.validation.constraints.NotNull;

public record ChangeLevelRequest(@NotNull MembershipLevel level) {
}
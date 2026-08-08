package com.cinemahub.cinemahub.membership.dto;

import com.cinemahub.cinemahub.membership.entity.MembershipLevel;

public record ChangeLevelRequest(MembershipLevel level) {
}
package com.cinemahub.cinemahub.membership.dto;

import com.cinemahub.cinemahub.membership.entity.Membership;
import com.cinemahub.cinemahub.membership.entity.MembershipLevel;

public record MembershipResponse(Long id, Long userId, MembershipLevel level, Integer points) {

    public static MembershipResponse from(Membership membership) {
        return new MembershipResponse(
                membership.getId(), membership.getUser().getId(), membership.getLevel(), membership.getPoints());
    }
}
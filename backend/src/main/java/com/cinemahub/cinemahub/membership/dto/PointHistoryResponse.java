package com.cinemahub.cinemahub.membership.dto;

import com.cinemahub.cinemahub.membership.entity.PointHistory;

import java.time.OffsetDateTime;

public record PointHistoryResponse(Long id, Integer points, String reason, OffsetDateTime createdAt) {

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getId(), history.getPoints(), history.getReason(), history.getCreatedAt());
    }
}
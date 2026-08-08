package com.cinemahub.cinemahub.membership.dto;

// delta puede ser negativo (redención de puntos); MembershipService.adjustPoints valida
// que el saldo resultante no quede negativo.
public record AdjustPointsRequest(int delta, String reason) {
}
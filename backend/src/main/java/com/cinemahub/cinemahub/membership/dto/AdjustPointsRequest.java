package com.cinemahub.cinemahub.membership.dto;

import jakarta.validation.constraints.Size;

// delta puede ser negativo (redención de puntos) o positivo; solo se prohíbe el cero,
// y eso ya lo valida MembershipService.adjustPoints (IllegalArgumentException) — no se
// puede expresar "distinto de cero" con una anotación estándar de Bean Validation.
public record AdjustPointsRequest(int delta, @Size(max = 150) String reason) {
}
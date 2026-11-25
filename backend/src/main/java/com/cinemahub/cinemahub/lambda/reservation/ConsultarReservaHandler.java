package com.cinemahub.cinemahub.lambda.reservation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConsultarReservaHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    long reservationId;
    try {
      reservationId = positiveLong(pathParameters(event).get("id"));
    } catch (RuntimeException e) {
      return error(400, "Bad Request", "id debe ser un entero positivo");
    }

    Map<?,?> claims = jwtClaims(event);
    String email = claims == null ? null : stringClaim(claims.get("email"));
    if (email == null || email.isBlank()) {
      return error(401, "Unauthorized", "Debes iniciar sesión");
    }

    try (Connection connection = DatabaseConnection.open()) {
      Long authenticatedUserId = findUserId(connection, email);
      if (authenticatedUserId == null) return error(404, "Not Found", "User no encontrado");

      Map<String,Object> reservation = findReservation(connection, reservationId);
      if (reservation == null) {
        return error(404, "Not Found", "Reservation no encontrada con id: " + reservationId);
      }
      long ownerId = ((Number) reservation.get("userId")).longValue();
      if (authenticatedUserId != ownerId && !isAdmin(claims)) {
        return error(403, "Forbidden", "No tienes permisos para consultar esta reserva");
      }
      return response(200, JSON.writeValueAsString(reservation));
    } catch (Exception e) {
      context.getLogger().log("Error consultando reserva: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo consultar la reserva");
    }
  }

  private Map<String,Object> pathParameters(Map<String,Object> event) {
    Object raw = event.get("pathParameters");
    if (!(raw instanceof Map<?,?> values)) return Map.of();
    Map<String,Object> result = new HashMap<>();
    values.forEach((key, value) -> { if (key != null) result.put(key.toString(), value); });
    return result;
  }

  private Map<?,?> jwtClaims(Map<String,Object> event) {
    Object rawContext = event.get("requestContext");
    if (!(rawContext instanceof Map<?,?> requestContext)) return null;
    Object rawAuthorizer = requestContext.get("authorizer");
    if (!(rawAuthorizer instanceof Map<?,?> authorizer)) return null;
    Object rawJwt = authorizer.get("jwt");
    if (!(rawJwt instanceof Map<?,?> jwt)) return null;
    Object rawClaims = jwt.get("claims");
    return rawClaims instanceof Map<?,?> claims ? claims : null;
  }

  private Long findUserId(Connection connection, String email) throws Exception {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT id FROM users WHERE lower(email) = lower(?)")) {
      statement.setString(1, email);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        return row.next() ? row.getLong("id") : null;
      }
    }
  }

  private Map<String,Object> findReservation(Connection connection, long reservationId) throws Exception {
    String sql = """
        SELECT id, user_id, status, expires_at, created_at
        FROM reservations
        WHERE id = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, reservationId);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) return null;
        Map<String,Object> reservation = new HashMap<>();
        reservation.put("id", row.getLong("id"));
        reservation.put("userId", row.getLong("user_id"));
        reservation.put("status", row.getString("status"));
        OffsetDateTime expiresAt = row.getObject("expires_at", OffsetDateTime.class);
        OffsetDateTime createdAt = row.getObject("created_at", OffsetDateTime.class);
        reservation.put("expiresAt", expiresAt == null ? null : expiresAt.toString());
        reservation.put("createdAt", createdAt == null ? null : createdAt.toString());
        return reservation;
      }
    }
  }

  private boolean isAdmin(Map<?,?> claims) {
    return containsAdmin(claims.get("roles")) || containsAdmin(claims.get("cognito:groups"));
  }

  private boolean containsAdmin(Object value) {
    if (value instanceof Collection<?> values) return values.stream().anyMatch(this::adminValue);
    if (value instanceof String text) {
      String normalized = text.trim();
      if (normalized.startsWith("[") && normalized.endsWith("]")) {
        normalized = normalized.substring(1, normalized.length() - 1);
      }
      for (String part : normalized.split("[, ]+")) if (adminValue(part)) return true;
    }
    return false;
  }

  private boolean adminValue(Object value) {
    if (value == null) return false;
    String role = value.toString().replace("\"", "").trim();
    return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
  }

  private String stringClaim(Object value) {
    return value == null ? null : value.toString();
  }

  private long positiveLong(Object value) {
    if (value == null) throw new IllegalArgumentException();
    long parsed = Long.parseLong(value.toString());
    if (parsed <= 0) throw new IllegalArgumentException();
    return parsed;
  }

  private static Map<String,Object> error(int status, String error, String message) {
    try {
      return response(status, JSON.writeValueAsString(Map.of(
          "timestamp", Instant.now().toString(), "status", status, "error", error, "message", message)));
    } catch (Exception ignored) {
      return response(500, "{\"status\":500}");
    }
  }

  private static Map<String,Object> response(int status, String body) {
    return Map.of("statusCode", status,
        "headers", Map.of("content-type", "application/json; charset=utf-8"),
        "isBase64Encoded", false, "body", body);
  }
}

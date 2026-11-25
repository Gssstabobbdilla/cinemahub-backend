package com.cinemahub.cinemahub.lambda.reservation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CancelarReservaHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
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
      connection.setAutoCommit(false);
      try {
        Long authenticatedUserId = findUserId(connection, email);
        if (authenticatedUserId == null) {
          connection.rollback();
          return error(404, "Not Found", "User no encontrado");
        }

        Reservation reservation = findReservationForUpdate(connection, reservationId);
        if (reservation == null) {
          connection.rollback();
          return error(404, "Not Found", "Reservation no encontrada con id: " + reservationId);
        }
        if (authenticatedUserId != reservation.userId() && !isAdmin(claims)) {
          connection.rollback();
          return error(403, "Forbidden", "No tienes permisos para cancelar esta reserva");
        }

        try (PreparedStatement delete = connection.prepareStatement(
            "DELETE FROM reservation_seats WHERE reservation_id = ?")) {
          delete.setLong(1, reservationId);
          delete.setQueryTimeout(10);
          delete.executeUpdate();
        }
        try (PreparedStatement update = connection.prepareStatement(
            "UPDATE reservations SET status = 'CANCELLED' WHERE id = ?")) {
          update.setLong(1, reservationId);
          update.setQueryTimeout(10);
          update.executeUpdate();
        }

        Map<String,Object> response = new HashMap<>();
        response.put("id", reservation.id());
        response.put("userId", reservation.userId());
        response.put("status", "CANCELLED");
        response.put("expiresAt", reservation.expiresAt() == null ? null : reservation.expiresAt().toString());
        response.put("createdAt", reservation.createdAt() == null ? null : reservation.createdAt().toString());
        connection.commit();
        return response(200, JSON.writeValueAsString(response));
      } catch (Exception e) {
        rollback(connection);
        throw e;
      }
    } catch (Exception e) {
      context.getLogger().log("Error cancelando reserva: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo cancelar la reserva");
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

  private Long findUserId(Connection connection, String email) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT id FROM users WHERE lower(email) = lower(?)")) {
      statement.setString(1, email);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        return row.next() ? row.getLong("id") : null;
      }
    }
  }

  private Reservation findReservationForUpdate(Connection connection, long id) throws SQLException {
    String sql = """
        SELECT id, user_id, expires_at, created_at
        FROM reservations
        WHERE id = ?
        FOR UPDATE
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) return null;
        return new Reservation(row.getLong("id"), row.getLong("user_id"),
            row.getObject("expires_at", OffsetDateTime.class),
            row.getObject("created_at", OffsetDateTime.class));
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

  private void rollback(Connection connection) {
    try { connection.rollback(); } catch (SQLException ignored) { }
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

  private record Reservation(long id, long userId, OffsetDateTime expiresAt, OffsetDateTime createdAt) {}
}

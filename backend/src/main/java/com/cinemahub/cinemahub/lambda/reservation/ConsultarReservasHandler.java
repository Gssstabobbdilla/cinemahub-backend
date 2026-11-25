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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsultarReservasHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    Long requestedUserId;
    try {
      requestedUserId = positiveLong(pathParameters(event).get("userId"));
    } catch (RuntimeException e) {
      return error(400, "Bad Request", "userId debe ser un entero positivo");
    }

    Map<?,?> claims = jwtClaims(event);
    String email = claims == null ? null : stringClaim(claims.get("email"));
    if (email == null || email.isBlank()) {
      return error(401, "Unauthorized", "Debes iniciar sesión");
    }

    try (Connection connection = DatabaseConnection.open()) {
      Long authenticatedUserId = findUserId(connection, email);
      if (authenticatedUserId == null) return error(404, "Not Found", "User no encontrado");
      if (!authenticatedUserId.equals(requestedUserId) && !isAdmin(claims)) {
        return error(403, "Forbidden", "No tienes permisos para consultar estas reservas");
      }
      return response(200, JSON.writeValueAsString(findReservations(connection, requestedUserId)));
    } catch (Exception e) {
      context.getLogger().log("Error consultando reservas: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudieron consultar las reservas");
    }
  }

  private Map<String,Object> pathParameters(Map<String,Object> event) {
    Object raw = event.get("pathParameters");
    return raw instanceof Map<?,?> values ? castMap(values) : Map.of();
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

  private List<Map<String,Object>> findReservations(Connection connection, long userId) throws Exception {
    String sql = """
        SELECT id, user_id, status, expires_at, created_at
        FROM reservations
        WHERE user_id = ?
        ORDER BY created_at DESC, id DESC
        """;
    List<Map<String,Object>> reservations = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, userId);
      statement.setQueryTimeout(10);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          Map<String,Object> item = new HashMap<>();
          item.put("id", rows.getLong("id"));
          item.put("userId", rows.getLong("user_id"));
          item.put("status", rows.getString("status"));
          OffsetDateTime expiresAt = rows.getObject("expires_at", OffsetDateTime.class);
          OffsetDateTime createdAt = rows.getObject("created_at", OffsetDateTime.class);
          item.put("expiresAt", expiresAt == null ? null : expiresAt.toString());
          item.put("createdAt", createdAt == null ? null : createdAt.toString());
          reservations.add(item);
        }
      }
    }
    return reservations;
  }

  private boolean isAdmin(Map<?,?> claims) {
    return containsAdmin(claims.get("roles")) || containsAdmin(claims.get("cognito:groups"));
  }

  private boolean containsAdmin(Object value) {
    if (value instanceof Collection<?> values) {
      return values.stream().anyMatch(this::adminValue);
    }
    if (value instanceof String text) {
      String normalized = text.trim();
      if (normalized.startsWith("[") && normalized.endsWith("]")) {
        normalized = normalized.substring(1, normalized.length() - 1);
      }
      for (String part : normalized.split("[, ]+")) {
        if (adminValue(part)) return true;
      }
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

  private Long positiveLong(Object value) {
    if (value == null) throw new IllegalArgumentException();
    long parsed = Long.parseLong(value.toString());
    if (parsed <= 0) throw new IllegalArgumentException();
    return parsed;
  }

  private Map<String,Object> castMap(Map<?,?> values) {
    Map<String,Object> result = new HashMap<>();
    values.forEach((key, value) -> { if (key != null) result.put(key.toString(), value); });
    return result;
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

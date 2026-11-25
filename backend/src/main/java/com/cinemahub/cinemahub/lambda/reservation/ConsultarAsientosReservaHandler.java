package com.cinemahub.cinemahub.lambda.reservation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsultarAsientosReservaHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
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
      Long ownerId = findReservationOwner(connection, reservationId);
      if (ownerId == null) {
        return error(404, "Not Found", "Reservation no encontrada con id: " + reservationId);
      }
      if (!authenticatedUserId.equals(ownerId) && !isAdmin(claims)) {
        return error(403, "Forbidden", "No tienes permisos para consultar los asientos de esta reserva");
      }
      return response(200, JSON.writeValueAsString(findSeats(connection, reservationId)));
    } catch (Exception e) {
      context.getLogger().log("Error consultando asientos de reserva: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudieron consultar los asientos de la reserva");
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

  private Long findReservationOwner(Connection connection, long reservationId) throws Exception {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT user_id FROM reservations WHERE id = ?")) {
      statement.setLong(1, reservationId);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        return row.next() ? row.getLong("user_id") : null;
      }
    }
  }

  private List<Map<String,Object>> findSeats(Connection connection, long reservationId) throws Exception {
    String sql = """
        SELECT s.id AS seat_id, s.row_label, s.seat_number, rs.price
        FROM reservation_seats rs
        JOIN seats s ON s.id = rs.seat_id
        WHERE rs.reservation_id = ?
        ORDER BY s.row_label, s.seat_number
        """;
    List<Map<String,Object>> seats = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, reservationId);
      statement.setQueryTimeout(10);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          Map<String,Object> seat = new HashMap<>();
          seat.put("seatId", rows.getLong("seat_id"));
          seat.put("rowLabel", rows.getString("row_label"));
          seat.put("seatNumber", rows.getInt("seat_number"));
          BigDecimal price = rows.getBigDecimal("price");
          seat.put("price", price);
          seats.add(seat);
        }
      }
    }
    return seats;
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

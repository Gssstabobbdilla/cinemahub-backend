package com.cinemahub.cinemahub.lambda.order;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CrearOrdenHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    long reservationId;
    try {
      reservationId = positiveLong(pathParameters(event).get("reservationId"));
    } catch (RuntimeException e) {
      return error(400, "Bad Request", "reservationId debe ser un entero positivo");
    }
    Map<?,?> claims = jwtClaims(event);
    String email = claims == null || claims.get("email") == null ? null : claims.get("email").toString();
    if (email == null || email.isBlank()) return error(401, "Unauthorized", "Debes iniciar sesión");

    try (Connection connection = DatabaseConnection.open()) {
      connection.setAutoCommit(false);
      try {
        Long userId = findUserId(connection, email);
        if (userId == null) return rollbackError(connection, 404, "Not Found", "User no encontrado");
        Long ownerId = reservationOwner(connection, reservationId);
        if (ownerId == null) return rollbackError(connection, 404, "Not Found",
            "Reservation no encontrada con id: " + reservationId);
        if (userId.longValue() != ownerId.longValue() && !isAdmin(claims)) {
          return rollbackError(connection, 403, "Forbidden", "No tienes permisos para crear una orden para esta reserva");
        }
        if (orderExists(connection, reservationId)) {
          return rollbackError(connection, 409, "Conflict", "Ya existe una orden para esta reserva");
        }
        BigDecimal total = seatsTotal(connection, reservationId);
        Map<String,Object> order = insertOrder(connection, reservationId, total);
        connection.commit();
        return response(201, JSON.writeValueAsString(order));
      } catch (SQLException e) {
        rollback(connection);
        if ("23505".equals(e.getSQLState())) {
          return error(409, "Conflict", "Ya existe una orden para esta reserva");
        }
        throw e;
      } catch (Exception e) {
        rollback(connection);
        throw e;
      }
    } catch (Exception e) {
      context.getLogger().log("Error creando orden desde reserva: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo crear la orden");
    }
  }

  private Map<String,Object> pathParameters(Map<String,Object> event) {
    Object raw = event.get("pathParameters");
    if (!(raw instanceof Map<?,?> params)) return Map.of();
    Map<String,Object> result = new HashMap<>();
    params.forEach((k,v) -> { if (k != null) result.put(k.toString(), v); });
    return result;
  }

  private Map<?,?> jwtClaims(Map<String,Object> event) {
    Object context = event.get("requestContext");
    if (!(context instanceof Map<?,?> rc)) return null;
    Object auth = rc.get("authorizer");
    if (!(auth instanceof Map<?,?> authorizer)) return null;
    Object rawJwt = authorizer.get("jwt");
    if (!(rawJwt instanceof Map<?,?> jwt)) return null;
    Object claims = jwt.get("claims");
    return claims instanceof Map<?,?> values ? values : null;
  }

  private Long findUserId(Connection c, String email) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT id FROM users WHERE lower(email)=lower(?)")) {
      s.setString(1, email); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) { return r.next() ? r.getLong("id") : null; }
    }
  }

  private Long reservationOwner(Connection c, long reservationId) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT user_id FROM reservations WHERE id=? FOR UPDATE")) {
      s.setLong(1, reservationId); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) { return r.next() ? r.getLong("user_id") : null; }
    }
  }

  private boolean orderExists(Connection c, long reservationId) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM orders WHERE reservation_id=?")) {
      s.setLong(1, reservationId); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) { return r.next(); }
    }
  }

  private BigDecimal seatsTotal(Connection c, long reservationId) throws SQLException {
    try (PreparedStatement s = c.prepareStatement(
        "SELECT COALESCE(SUM(price),0) AS total FROM reservation_seats WHERE reservation_id=?")) {
      s.setLong(1, reservationId); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) { r.next(); return r.getBigDecimal("total"); }
    }
  }

  private Map<String,Object> insertOrder(Connection c, long reservationId, BigDecimal total) throws SQLException {
    String sql = """
        INSERT INTO orders (reservation_id,total,status) VALUES (?,?,'PENDING')
        RETURNING id,reservation_id,total,status,purchased_at
        """;
    try (PreparedStatement s = c.prepareStatement(sql)) {
      s.setLong(1, reservationId); s.setBigDecimal(2, total); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) {
        if (!r.next()) throw new SQLException("No se pudo obtener la orden creada");
        Map<String,Object> result = new HashMap<>();
        result.put("id", r.getLong("id"));
        result.put("reservationId", r.getLong("reservation_id"));
        result.put("total", r.getBigDecimal("total"));
        result.put("status", r.getString("status"));
        OffsetDateTime purchasedAt = r.getObject("purchased_at", OffsetDateTime.class);
        result.put("purchasedAt", purchasedAt == null ? null : purchasedAt.toString());
        return result;
      }
    }
  }

  private boolean isAdmin(Map<?,?> claims) {
    return admin(claims.get("roles")) || admin(claims.get("cognito:groups"));
  }

  private boolean admin(Object raw) {
    if (raw instanceof Collection<?> values) return values.stream().anyMatch(this::isAdminRole);
    if (raw instanceof String text) {
      String normalized = text.trim();
      if (normalized.startsWith("[") && normalized.endsWith("]")) normalized = normalized.substring(1, normalized.length()-1);
      for (String value : normalized.split("[, ]+")) if (isAdminRole(value)) return true;
    }
    return false;
  }

  private boolean isAdminRole(Object raw) {
    if (raw == null) return false;
    String role = raw.toString().replace("\"", "").trim();
    return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
  }

  private long positiveLong(Object raw) {
    if (raw == null) throw new IllegalArgumentException();
    long value = Long.parseLong(raw.toString());
    if (value <= 0) throw new IllegalArgumentException();
    return value;
  }

  private Map<String,Object> rollbackError(Connection c, int status, String type, String message) {
    rollback(c); return error(status, type, message);
  }

  private void rollback(Connection c) {
    try { c.rollback(); } catch (SQLException ignored) { }
  }

  private static Map<String,Object> error(int status, String type, String message) {
    try { return response(status, JSON.writeValueAsString(Map.of("timestamp", Instant.now().toString(),
        "status", status, "error", type, "message", message))); }
    catch (Exception ignored) { return response(500, "{\"status\":500}"); }
  }

  private static Map<String,Object> response(int status, String body) {
    return Map.of("statusCode", status, "headers", Map.of("content-type", "application/json; charset=utf-8"),
        "isBase64Encoded", false, "body", body);
  }
}

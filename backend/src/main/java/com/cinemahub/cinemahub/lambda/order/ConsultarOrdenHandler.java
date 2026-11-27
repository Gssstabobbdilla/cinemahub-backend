package com.cinemahub.cinemahub.lambda.order;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConsultarOrdenHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    long orderId;
    try {
      orderId = positiveLong(pathParameters(event).get("id"));
    } catch (RuntimeException e) {
      return error(400, "Bad Request", "id debe ser un entero positivo");
    }

    Map<?,?> claims = jwtClaims(event);
    String email = claims == null || claims.get("email") == null ? null : claims.get("email").toString();
    if (email == null || email.isBlank()) return error(401, "Unauthorized", "Debes iniciar sesión");

    try (Connection connection = DatabaseConnection.open()) {
      Long userId = findUserId(connection, email);
      if (userId == null) return error(404, "Not Found", "User no encontrado");
      OrderSnapshot order = findOrder(connection, orderId);
      if (order == null) return error(404, "Not Found", "Order no encontrada con id: " + orderId);
      if (userId.longValue() != order.userId() && !isAdmin(claims)) {
        return error(403, "Forbidden", "No tienes permisos para consultar esta orden");
      }
      return response(200, JSON.writeValueAsString(order.response()));
    } catch (Exception e) {
      context.getLogger().log("Error consultando orden: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo consultar la orden");
    }
  }

  private Map<String,Object> pathParameters(Map<String,Object> event) {
    Object raw = event.get("pathParameters");
    if (!(raw instanceof Map<?,?> params)) return Map.of();
    Map<String,Object> result = new HashMap<>();
    params.forEach((key,value) -> { if (key != null) result.put(key.toString(), value); });
    return result;
  }

  private Map<?,?> jwtClaims(Map<String,Object> event) {
    Object rawContext = event.get("requestContext");
    if (!(rawContext instanceof Map<?,?> rc)) return null;
    Object rawAuth = rc.get("authorizer");
    if (!(rawAuth instanceof Map<?,?> auth)) return null;
    Object rawJwt = auth.get("jwt");
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

  private OrderSnapshot findOrder(Connection connection, long orderId) throws Exception {
    String sql = """
        SELECT o.id, o.reservation_id, r.user_id, o.total, o.status, o.purchased_at
        FROM orders o
        JOIN reservations r ON r.id = o.reservation_id
        WHERE o.id = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, orderId);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) return null;
        OffsetDateTime purchasedAt = row.getObject("purchased_at", OffsetDateTime.class);
        return new OrderSnapshot(row.getLong("id"), row.getLong("reservation_id"),
            row.getLong("user_id"), row.getBigDecimal("total"), row.getString("status"), purchasedAt);
      }
    }
  }

  private boolean isAdmin(Map<?,?> claims) {
    return containsAdmin(claims.get("roles")) || containsAdmin(claims.get("cognito:groups"));
  }

  private boolean containsAdmin(Object raw) {
    if (raw instanceof Collection<?> values) return values.stream().anyMatch(this::adminRole);
    if (raw instanceof String text) {
      String normalized = text.trim();
      if (normalized.startsWith("[") && normalized.endsWith("]")) {
        normalized = normalized.substring(1, normalized.length() - 1);
      }
      for (String part : normalized.split("[, ]+")) if (adminRole(part)) return true;
    }
    return false;
  }

  private boolean adminRole(Object raw) {
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

  private static Map<String,Object> error(int status, String type, String message) {
    try {
      return response(status, JSON.writeValueAsString(Map.of("timestamp", Instant.now().toString(),
          "status", status, "error", type, "message", message)));
    } catch (Exception ignored) { return response(500, "{\"status\":500}"); }
  }

  private static Map<String,Object> response(int status, String body) {
    return Map.of("statusCode", status, "headers", Map.of("content-type", "application/json; charset=utf-8"),
        "isBase64Encoded", false, "body", body);
  }

  private record OrderSnapshot(long id, long reservationId, long userId, BigDecimal total,
                               String status, OffsetDateTime purchasedAt) {
    Map<String,Object> response() {
      Map<String,Object> body = new HashMap<>();
      body.put("id", id);
      body.put("reservationId", reservationId);
      body.put("total", total);
      body.put("status", status);
      body.put("purchasedAt", purchasedAt == null ? null : purchasedAt.toString());
      return body;
    }
  }
}

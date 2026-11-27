package com.cinemahub.cinemahub.lambda.order;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AgregarProductoOrdenHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    long orderId;
    try {
      orderId = positiveLong(pathParameters(event).get("id"), "id");
    } catch (ApiException e) {
      return error(e.status, e.type, e.getMessage());
    }
    Map<?,?> claims = jwtClaims(event);
    String email = claims == null || claims.get("email") == null ? null : claims.get("email").toString();
    if (email == null || email.isBlank()) return error(401, "Unauthorized", "Debes iniciar sesión");

    AddProductRequest request;
    try {
      request = parseRequest(event);
    } catch (ApiException e) {
      return error(e.status, e.type, e.getMessage());
    } catch (Exception e) {
      return error(400, "Bad Request", "El cuerpo de la solicitud no es válido");
    }

    try (Connection connection = DatabaseConnection.open()) {
      connection.setAutoCommit(false);
      try {
        Long userId = findUserId(connection, email);
        if (userId == null) return rollbackError(connection, 404, "Not Found", "User no encontrado");
        OrderSnapshot order = findOrderForUpdate(connection, orderId);
        if (order == null) return rollbackError(connection, 404, "Not Found", "Order no encontrada con id: " + orderId);
        if (userId.longValue() != order.userId() && !isAdmin(claims)) {
          return rollbackError(connection, 403, "Forbidden", "No tienes permisos para modificar esta orden");
        }
        if (!"PENDING".equals(order.status())) {
          return rollbackError(connection, 409, "Conflict", "Solo se pueden agregar productos a una orden PENDING");
        }

        ProductSnapshot product = findProductForUpdate(connection, request.productId());
        if (product == null) {
          return rollbackError(connection, 404, "Not Found", "Product no encontrado con id: " + request.productId());
        }
        if (product.stock() < request.quantity()) {
          return rollbackError(connection, 409, "Conflict", "Stock insuficiente para " + product.name());
        }

        OrderProductSnapshot current = findOrderProduct(connection, orderId, product.id());
        int cumulativeQuantity;
        BigDecimal unitPrice;
        if (current == null) {
          cumulativeQuantity = request.quantity();
          unitPrice = product.price();
          insertOrderProduct(connection, orderId, product.id(), cumulativeQuantity, unitPrice);
        } else {
          try {
            cumulativeQuantity = Math.addExact(current.quantity(), request.quantity());
          } catch (ArithmeticException e) {
            return rollbackError(connection, 400, "Bad Request", "La cantidad acumulada excede el límite permitido");
          }
          unitPrice = current.unitPrice();
          updateOrderProduct(connection, orderId, product.id(), cumulativeQuantity);
        }

        updateStock(connection, product.id(), product.stock() - request.quantity());
        updateOrderTotal(connection, orderId,
            order.total().add(product.price().multiply(BigDecimal.valueOf(request.quantity()))));
        connection.commit();

        Map<String,Object> responseBody = new HashMap<>();
        responseBody.put("productId", product.id());
        responseBody.put("productName", product.name());
        responseBody.put("quantity", cumulativeQuantity);
        responseBody.put("unitPrice", unitPrice);
        responseBody.put("lineTotal", unitPrice.multiply(BigDecimal.valueOf(cumulativeQuantity)));
        return response(201, JSON.writeValueAsString(responseBody));
      } catch (SQLException e) {
        rollback(connection);
        throw e;
      } catch (Exception e) {
        rollback(connection);
        throw e;
      }
    } catch (Exception e) {
      context.getLogger().log("Error agregando producto a orden: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo agregar el producto a la orden");
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

  private AddProductRequest parseRequest(Map<String,Object> event) throws Exception {
    Object raw = event.get("body");
    if (!(raw instanceof String body) || body.isBlank()) {
      throw new ApiException(400, "Bad Request", "El cuerpo es obligatorio");
    }
    if (Boolean.TRUE.equals(event.get("isBase64Encoded"))) {
      body = new String(Base64.getDecoder().decode(body), java.nio.charset.StandardCharsets.UTF_8);
    }
    Map<String,Object> values = JSON.readValue(body, new TypeReference<>() {});
    long productId = positiveLong(values.get("productId"), "productId");
    long quantity = positiveLong(values.get("quantity"), "quantity");
    if (quantity > Integer.MAX_VALUE) {
      throw new ApiException(400, "Bad Request", "quantity excede el límite permitido");
    }
    return new AddProductRequest(productId, (int) quantity);
  }

  private Long findUserId(Connection c, String email) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT id FROM users WHERE lower(email)=lower(?)")) {
      s.setString(1, email); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) { return r.next() ? r.getLong("id") : null; }
    }
  }

  private OrderSnapshot findOrderForUpdate(Connection c, long id) throws SQLException {
    String sql = """
        SELECT o.id, r.user_id, o.status, o.total
        FROM orders o JOIN reservations r ON r.id = o.reservation_id
        WHERE o.id = ?
        FOR UPDATE OF o
        """;
    try (PreparedStatement s = c.prepareStatement(sql)) {
      s.setLong(1, id); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) {
        return r.next() ? new OrderSnapshot(r.getLong("id"), r.getLong("user_id"),
            r.getString("status"), r.getBigDecimal("total")) : null;
      }
    }
  }

  private ProductSnapshot findProductForUpdate(Connection c, long id) throws SQLException {
    try (PreparedStatement s = c.prepareStatement(
        "SELECT id,name,price,stock FROM products WHERE id=? FOR UPDATE")) {
      s.setLong(1, id); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) {
        return r.next() ? new ProductSnapshot(r.getLong("id"), r.getString("name"),
            r.getBigDecimal("price"), r.getInt("stock")) : null;
      }
    }
  }

  private OrderProductSnapshot findOrderProduct(Connection c, long orderId, long productId) throws SQLException {
    try (PreparedStatement s = c.prepareStatement(
        "SELECT quantity,unit_price FROM order_products WHERE order_id=? AND product_id=?")) {
      s.setLong(1, orderId); s.setLong(2, productId); s.setQueryTimeout(10);
      try (ResultSet r = s.executeQuery()) {
        return r.next() ? new OrderProductSnapshot(r.getInt("quantity"), r.getBigDecimal("unit_price")) : null;
      }
    }
  }

  private void insertOrderProduct(Connection c, long orderId, long productId, int quantity, BigDecimal price)
      throws SQLException {
    try (PreparedStatement s = c.prepareStatement(
        "INSERT INTO order_products (order_id,product_id,quantity,unit_price) VALUES (?,?,?,?)")) {
      s.setLong(1, orderId); s.setLong(2, productId); s.setInt(3, quantity); s.setBigDecimal(4, price);
      s.setQueryTimeout(10); s.executeUpdate();
    }
  }

  private void updateOrderProduct(Connection c, long orderId, long productId, int quantity) throws SQLException {
    try (PreparedStatement s = c.prepareStatement(
        "UPDATE order_products SET quantity=? WHERE order_id=? AND product_id=?")) {
      s.setInt(1, quantity); s.setLong(2, orderId); s.setLong(3, productId);
      s.setQueryTimeout(10); s.executeUpdate();
    }
  }

  private void updateStock(Connection c, long productId, int stock) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("UPDATE products SET stock=? WHERE id=?")) {
      s.setInt(1, stock); s.setLong(2, productId); s.setQueryTimeout(10); s.executeUpdate();
    }
  }

  private void updateOrderTotal(Connection c, long orderId, BigDecimal total) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("UPDATE orders SET total=? WHERE id=?")) {
      s.setBigDecimal(1, total); s.setLong(2, orderId); s.setQueryTimeout(10); s.executeUpdate();
    }
  }

  private boolean isAdmin(Map<?,?> claims) {
    return containsAdmin(claims.get("roles")) || containsAdmin(claims.get("cognito:groups"));
  }

  private boolean containsAdmin(Object raw) {
    if (raw instanceof Collection<?> values) return values.stream().anyMatch(this::adminRole);
    if (raw instanceof String text) {
      String normalized = text.trim();
      if (normalized.startsWith("[") && normalized.endsWith("]")) normalized = normalized.substring(1, normalized.length()-1);
      for (String part : normalized.split("[, ]+")) if (adminRole(part)) return true;
    }
    return false;
  }

  private boolean adminRole(Object raw) {
    if (raw == null) return false;
    String role = raw.toString().replace("\"", "").trim();
    return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
  }

  private long positiveLong(Object raw, String name) {
    if (raw == null) throw new ApiException(400, "Bad Request", "Falta el campo " + name);
    try {
      long value = new BigDecimal(raw.toString()).longValueExact();
      if (value <= 0) throw new ArithmeticException();
      return value;
    } catch (NumberFormatException | ArithmeticException e) {
      throw new ApiException(400, "Bad Request", name + " debe ser un entero positivo");
    }
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

  private record AddProductRequest(long productId, int quantity) {}
  private record OrderSnapshot(long id, long userId, String status, BigDecimal total) {}
  private record ProductSnapshot(long id, String name, BigDecimal price, int stock) {}
  private record OrderProductSnapshot(int quantity, BigDecimal unitPrice) {}
  private static class ApiException extends RuntimeException {
    private final int status;
    private final String type;
    private ApiException(int status, String type, String message) { super(message); this.status = status; this.type = type; }
  }
}

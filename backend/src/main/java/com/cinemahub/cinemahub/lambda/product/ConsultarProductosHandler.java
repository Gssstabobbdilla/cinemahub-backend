package com.cinemahub.cinemahub.lambda.product;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConsultarProductosHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
  private static final List<String> STATUSES = List.of("ACTIVE", "INACTIVE", "OUT_OF_STOCK");

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    Map<String,String> query = queryParameters(event);
    Long categoryId;
    try {
      categoryId = optionalPositiveLong(query.get("categoryId"));
    } catch (RuntimeException e) {
      return error(400, "Bad Request", "categoryId debe ser un entero positivo");
    }

    String status = query.get("status");
    if (status != null && !STATUSES.contains(status)) {
      return error(400, "Bad Request", "status debe ser ACTIVE, INACTIVE o OUT_OF_STOCK");
    }
    if (categoryId == null && status == null) {
      return error(400, "Bad Request", "Se requiere categoryId o status");
    }

    try (Connection connection = DatabaseConnection.open()) {
      List<Map<String,Object>> products = findProducts(connection, categoryId, status);
      return response(200, JSON.writeValueAsString(products));
    } catch (Exception e) {
      context.getLogger().log("Error consultando productos: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudieron consultar los productos");
    }
  }

  private Map<String,String> queryParameters(Map<String,Object> event) {
    Object raw = event.get("queryStringParameters");
    if (!(raw instanceof Map<?,?> values)) return Map.of();
    Map<String,String> result = new HashMap<>();
    values.forEach((key, value) -> {
      if (key != null && value != null) result.put(key.toString(), value.toString());
    });
    return result;
  }

  private List<Map<String,Object>> findProducts(Connection connection, Long categoryId, String status)
      throws Exception {
    StringBuilder sql = new StringBuilder("""
        SELECT p.id, p.category_id, c.name AS category_name, p.name, p.description,
               p.price, p.stock, p.status, p.image_url
        FROM products p
        JOIN product_categories c ON c.id = p.category_id
        """);
    if (categoryId != null) sql.append(" WHERE p.category_id = ?");
    else sql.append(" WHERE p.status = ?");
    sql.append(" ORDER BY p.id");

    List<Map<String,Object>> products = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
      if (categoryId != null) statement.setLong(1, categoryId);
      else statement.setString(1, status);
      statement.setQueryTimeout(10);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          Map<String,Object> product = new HashMap<>();
          product.put("id", rows.getLong("id"));
          product.put("categoryId", rows.getLong("category_id"));
          product.put("categoryName", rows.getString("category_name"));
          product.put("name", rows.getString("name"));
          product.put("description", rows.getString("description"));
          BigDecimal price = rows.getBigDecimal("price");
          product.put("price", price);
          product.put("stock", rows.getInt("stock"));
          product.put("status", rows.getString("status"));
          product.put("imageUrl", rows.getString("image_url"));
          products.add(product);
        }
      }
    }
    return products;
  }

  private Long optionalPositiveLong(String raw) {
    if (raw == null) return null;
    if (raw.isBlank()) throw new IllegalArgumentException();
    long value = Long.parseLong(raw);
    if (value <= 0) throw new IllegalArgumentException();
    return value;
  }

  private static Map<String,Object> error(int status, String type, String message) {
    try {
      return response(status, JSON.writeValueAsString(Map.of(
          "timestamp", Instant.now().toString(), "status", status, "error", type, "message", message)));
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

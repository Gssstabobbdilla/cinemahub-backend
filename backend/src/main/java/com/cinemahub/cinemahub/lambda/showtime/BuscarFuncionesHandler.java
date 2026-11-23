package com.cinemahub.cinemahub.lambda.showtime;

import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuscarFuncionesHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String SELECT = """
      SELECT s.id, s.movie_id, m.title AS movie_title, s.room_id, r.name AS room_name,
             c.name AS cinema_name, s.show_date, s.start_time, s.end_time, s.language,
             s.format, s.base_price, s.status
      FROM showtimes s
      JOIN movies m ON m.id = s.movie_id
      JOIN rooms r ON r.id = s.room_id
      JOIN cinemas c ON c.id = r.cinema_id
      """;

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    try {
      Long movieId = optionalLong(event, "movieId");
      Long roomId = optionalLong(event, "roomId");
      LocalDate date = optionalDate(event, "date");
      if (movieId == null && (roomId == null || date == null)) {
        return error(400, "Bad Request", "Se requiere movieId, o roomId junto con date");
      }
      return response(200, JSON.writeValueAsString(find(movieId, roomId, date)));
    } catch (IllegalArgumentException e) {
      return error(400, "Bad Request", e.getMessage());
    } catch (Exception e) {
      context.getLogger().log("Error buscando funciones: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudieron consultar las funciones");
    }
  }

  private List<Map<String,Object>> find(Long movieId, Long roomId, LocalDate date) throws Exception {
    String sql = SELECT + (movieId != null
        ? " WHERE s.movie_id = ? ORDER BY s.show_date, s.start_time, s.id"
        : " WHERE s.room_id = ? AND s.show_date = ? ORDER BY s.start_time, s.id");
    try (Connection connection = DatabaseConnection.open();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      if (movieId != null) {
        statement.setLong(1, movieId);
      } else {
        statement.setLong(1, roomId);
        statement.setObject(2, date);
      }
      statement.setQueryTimeout(15);
      try (ResultSet rows = statement.executeQuery()) {
        List<Map<String,Object>> result = new ArrayList<>();
        while (rows.next()) {
          Map<String,Object> item = new HashMap<>();
          item.put("id", rows.getLong("id"));
          item.put("movieId", rows.getLong("movie_id"));
          item.put("movieTitle", rows.getString("movie_title"));
          item.put("roomId", rows.getLong("room_id"));
          item.put("roomName", rows.getString("room_name"));
          item.put("cinemaName", rows.getString("cinema_name"));
          item.put("showDate", rows.getDate("show_date").toLocalDate().toString());
          item.put("startTime", rows.getTime("start_time").toLocalTime().toString());
          item.put("endTime", rows.getTime("end_time").toLocalTime().toString());
          item.put("language", rows.getString("language"));
          item.put("format", rows.getString("format"));
          item.put("basePrice", rows.getBigDecimal("base_price"));
          item.put("status", rows.getString("status"));
          result.add(item);
        }
        return result;
      }
    }
  }

  private static Long optionalLong(Map<String,Object> event, String name) {
    String value = query(event, name);
    if (value == null || value.isBlank()) return null;
    try { return Long.parseLong(value); }
    catch (NumberFormatException e) { throw new IllegalArgumentException("El parámetro " + name + " debe ser un entero"); }
  }

  private static LocalDate optionalDate(Map<String,Object> event, String name) {
    String value = query(event, name);
    if (value == null || value.isBlank()) return null;
    try { return LocalDate.parse(value); }
    catch (Exception e) { throw new IllegalArgumentException("El parámetro date debe usar el formato YYYY-MM-DD"); }
  }

  private static String query(Map<String,Object> event, String name) {
    Object parameters = event.get("queryStringParameters");
    if (!(parameters instanceof Map<?,?> map) || map.get(name) == null) return null;
    return map.get(name).toString();
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

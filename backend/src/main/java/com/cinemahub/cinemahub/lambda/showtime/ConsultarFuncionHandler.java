package com.cinemahub.cinemahub.lambda.showtime;

import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ConsultarFuncionHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String SQL = """
      SELECT s.id, s.movie_id, m.title AS movie_title, s.room_id, r.name AS room_name,
             c.name AS cinema_name, s.show_date, s.start_time, s.end_time, s.language,
             s.format, s.base_price, s.status
      FROM showtimes s
      JOIN movies m ON m.id = s.movie_id
      JOIN rooms r ON r.id = s.room_id
      JOIN cinemas c ON c.id = r.cinema_id
      WHERE s.id = ?
      """;

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    Long id;
    try {
      id = pathId(event);
    } catch (IllegalArgumentException e) {
      return error(400, "Bad Request", e.getMessage());
    }

    try {
      Map<String,Object> showtime = findById(id);
      if (showtime == null) {
        return error(404, "Not Found", "Showtime no encontrado con id: " + id);
      }
      return response(200, JSON.writeValueAsString(showtime));
    } catch (Exception e) {
      context.getLogger().log("Error consultando función: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo consultar la función");
    }
  }

  private Map<String,Object> findById(Long id) throws Exception {
    try (Connection connection = DatabaseConnection.open();
         PreparedStatement statement = connection.prepareStatement(SQL)) {
      statement.setLong(1, id);
      statement.setQueryTimeout(15);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) return null;
        Map<String,Object> result = new HashMap<>();
        result.put("id", row.getLong("id"));
        result.put("movieId", row.getLong("movie_id"));
        result.put("movieTitle", row.getString("movie_title"));
        result.put("roomId", row.getLong("room_id"));
        result.put("roomName", row.getString("room_name"));
        result.put("cinemaName", row.getString("cinema_name"));
        result.put("showDate", row.getDate("show_date").toLocalDate().toString());
        result.put("startTime", row.getTime("start_time").toLocalTime().toString());
        result.put("endTime", row.getTime("end_time").toLocalTime().toString());
        result.put("language", row.getString("language"));
        result.put("format", row.getString("format"));
        result.put("basePrice", row.getBigDecimal("base_price"));
        result.put("status", row.getString("status"));
        return result;
      }
    }
  }

  private static Long pathId(Map<String,Object> event) {
    Object raw = event.get("pathParameters");
    if (!(raw instanceof Map<?,?> parameters) || parameters.get("id") == null) {
      throw new IllegalArgumentException("Falta el id de la función");
    }
    try { return Long.parseLong(parameters.get("id").toString()); }
    catch (NumberFormatException e) { throw new IllegalArgumentException("El id de la función debe ser un entero"); }
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

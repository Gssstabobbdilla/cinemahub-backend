package com.cinemahub.cinemahub.lambda.reservation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cinemahub.cinemahub.lambda.shared.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsultarAsientosHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String FIND_SHOWTIME = "SELECT id, room_id FROM showtimes WHERE id = ?";
  private static final String FIND_SEATS = """
      SELECT s.id AS seat_id, s.row_label, s.seat_number, s.seat_type,
             (rs.seat_id IS NOT NULL) AS taken
      FROM seats s
      LEFT JOIN reservation_seats rs
        ON rs.showtime_id = ? AND rs.seat_id = s.id
      WHERE s.room_id = ?
      ORDER BY s.row_label, s.seat_number
      """;

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    Long showtimeId;
    try {
      showtimeId = pathId(event);
    } catch (IllegalArgumentException e) {
      return error(400, "Bad Request", e.getMessage());
    }

    try {
      return response(200, JSON.writeValueAsString(findAvailability(showtimeId)));
    } catch (ShowtimeNotFoundException e) {
      return error(404, "Not Found", "Showtime no encontrado con id: " + showtimeId);
    } catch (Exception e) {
      context.getLogger().log("Error consultando asientos: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo consultar la disponibilidad de asientos");
    }
  }

  private List<Map<String,Object>> findAvailability(Long showtimeId) throws Exception {
    try (Connection connection = DatabaseConnection.open()) {
      long roomId;
      try (PreparedStatement showtimeQuery = connection.prepareStatement(FIND_SHOWTIME)) {
        showtimeQuery.setLong(1, showtimeId);
        showtimeQuery.setQueryTimeout(10);
        try (ResultSet showtime = showtimeQuery.executeQuery()) {
          if (!showtime.next()) throw new ShowtimeNotFoundException();
          roomId = showtime.getLong("room_id");
        }
      }

      try (PreparedStatement seatsQuery = connection.prepareStatement(FIND_SEATS)) {
        seatsQuery.setLong(1, showtimeId);
        seatsQuery.setLong(2, roomId);
        seatsQuery.setQueryTimeout(15);
        try (ResultSet rows = seatsQuery.executeQuery()) {
          List<Map<String,Object>> seats = new ArrayList<>();
          while (rows.next()) {
            Map<String,Object> seat = new HashMap<>();
            seat.put("seatId", rows.getLong("seat_id"));
            seat.put("rowLabel", rows.getString("row_label"));
            seat.put("seatNumber", rows.getInt("seat_number"));
            seat.put("seatType", rows.getString("seat_type"));
            seat.put("taken", rows.getBoolean("taken"));
            seats.add(seat);
          }
          return seats;
        }
      }
    }
  }

  private static Long pathId(Map<String,Object> event) {
    Object raw = event.get("pathParameters");
    if (!(raw instanceof Map<?,?> parameters) || parameters.get("showtimeId") == null) {
      throw new IllegalArgumentException("Falta el id de la función");
    }
    try { return Long.parseLong(parameters.get("showtimeId").toString()); }
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

  private static class ShowtimeNotFoundException extends RuntimeException {
  }
}

package com.cinemahub.cinemahub.lambda.reservation;

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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistrarReservaHandler implements RequestHandler<Map<String,Object>,Map<String,Object>> {
  private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

  @Override public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
    String email = authenticatedEmail(event);
    if (email == null || email.isBlank()) {
      return error(401, "Unauthorized", "Debes iniciar sesión");
    }

    ReservationRequest request;
    try {
      request = parseRequest(event);
    } catch (ApiException e) {
      return error(e.status, e.error, e.getMessage());
    } catch (Exception e) {
      return error(400, "Bad Request", "El cuerpo de la solicitud no es válido");
    }

    try (Connection connection = DatabaseConnection.open()) {
      connection.setAutoCommit(false);
      try {
        Map<String,Object> reservation = createReservation(connection, email, request);
        connection.commit();
        return response(201, JSON.writeValueAsString(reservation));
      } catch (ApiException e) {
        rollback(connection);
        return error(e.status, e.error, e.getMessage());
      } catch (SQLException e) {
        rollback(connection);
        if ("23505".equals(e.getSQLState())) {
          return error(409, "Conflict", "Uno o más asientos ya fueron reservados para esta función");
        }
        throw e;
      } catch (Exception e) {
        rollback(connection);
        throw e;
      }
    } catch (Exception e) {
      context.getLogger().log("Error registrando reserva: " + e.getClass().getSimpleName());
      return error(500, "Internal Server Error", "No se pudo registrar la reserva");
    }
  }

  private Map<String,Object> createReservation(Connection connection, String email, ReservationRequest request)
      throws Exception {
    long userId = findUserId(connection, email);
    Showtime showtime = findShowtime(connection, request.showtimeId());
    validateSeatsBelongToShowtimeRoom(connection, showtime.roomId(), request.seatIds());
    validateSeatsAreAvailable(connection, request.showtimeId(), request.seatIds());

    long reservationId;
    String status;
    OffsetDateTime expiresAt;
    OffsetDateTime createdAt;
    String insertReservation = """
        INSERT INTO reservations (user_id, status, expires_at)
        VALUES (?, 'PENDING', now() + INTERVAL '10 minutes')
        RETURNING id, status, expires_at, created_at
        """;
    try (PreparedStatement statement = connection.prepareStatement(insertReservation)) {
      statement.setLong(1, userId);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) throw new SQLException("No se pudo obtener la reserva creada");
        reservationId = row.getLong("id");
        status = row.getString("status");
        expiresAt = row.getObject("expires_at", OffsetDateTime.class);
        createdAt = row.getObject("created_at", OffsetDateTime.class);
      }
    }

    String insertSeat = """
        INSERT INTO reservation_seats (reservation_id, showtime_id, seat_id, price)
        VALUES (?, ?, ?, ?)
        """;
    try (PreparedStatement statement = connection.prepareStatement(insertSeat)) {
      for (Long seatId : request.seatIds()) {
        statement.setLong(1, reservationId);
        statement.setLong(2, request.showtimeId());
        statement.setLong(3, seatId);
        statement.setBigDecimal(4, showtime.basePrice());
        statement.addBatch();
      }
      statement.executeBatch();
    }

    Map<String,Object> result = new HashMap<>();
    result.put("id", reservationId);
    result.put("userId", userId);
    result.put("status", status);
    result.put("expiresAt", expiresAt.toString());
    result.put("createdAt", createdAt.toString());
    return result;
  }

  private long findUserId(Connection connection, String email) throws Exception {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT id FROM users WHERE lower(email) = lower(?)")) {
      statement.setString(1, email);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) throw new ApiException(404, "Not Found", "User no encontrado");
        return row.getLong("id");
      }
    }
  }

  private Showtime findShowtime(Connection connection, long showtimeId) throws Exception {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT id, room_id, base_price FROM showtimes WHERE id = ?")) {
      statement.setLong(1, showtimeId);
      statement.setQueryTimeout(10);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          throw new ApiException(404, "Not Found", "Showtime no encontrado con id: " + showtimeId);
        }
        return new Showtime(row.getLong("room_id"), row.getBigDecimal("base_price"));
      }
    }
  }

  private void validateSeatsBelongToShowtimeRoom(Connection connection, long roomId, List<Long> seatIds)
      throws Exception {
    String placeholders = String.join(",", java.util.Collections.nCopies(seatIds.size(), "?"));
    String sql = "SELECT id, room_id FROM seats WHERE id IN (" + placeholders + ")";
    Map<Long,Long> seatRooms = new HashMap<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      for (int i = 0; i < seatIds.size(); i++) statement.setLong(i + 1, seatIds.get(i));
      statement.setQueryTimeout(10);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) seatRooms.put(rows.getLong("id"), rows.getLong("room_id"));
      }
    }
    for (Long seatId : seatIds) {
      Long seatRoom = seatRooms.get(seatId);
      if (seatRoom == null) throw new ApiException(404, "Not Found", "Seat no encontrado con id: " + seatId);
      if (seatRoom != roomId) {
        throw new ApiException(400, "Bad Request", "El asiento " + seatId + " no pertenece a la sala de esta función");
      }
    }
  }

  private void validateSeatsAreAvailable(Connection connection, long showtimeId, List<Long> seatIds)
      throws Exception {
    String placeholders = String.join(",", java.util.Collections.nCopies(seatIds.size(), "?"));
    String sql = """
        SELECT rs.seat_id
        FROM reservation_seats rs
        JOIN reservations r ON r.id = rs.reservation_id
        WHERE rs.showtime_id = ?
          AND rs.seat_id IN (%s)
          AND r.status IN ('PENDING', 'CONFIRMED')
        """.formatted(placeholders);
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, showtimeId);
      for (int i = 0; i < seatIds.size(); i++) statement.setLong(i + 2, seatIds.get(i));
      statement.setQueryTimeout(10);
      try (ResultSet rows = statement.executeQuery()) {
        if (rows.next()) {
          throw new ApiException(409, "Conflict",
              "Existe una reserva para seatId (función " + showtimeId + "): " + rows.getLong("seat_id"));
        }
      }
    }
  }

  private ReservationRequest parseRequest(Map<String,Object> event) throws Exception {
    Object rawBody = event.get("body");
    if (!(rawBody instanceof String body) || body.isBlank()) {
      throw new ApiException(400, "Bad Request", "El cuerpo es obligatorio");
    }
    if (Boolean.TRUE.equals(event.get("isBase64Encoded"))) {
      body = new String(Base64.getDecoder().decode(body), java.nio.charset.StandardCharsets.UTF_8);
    }
    Map<String,Object> values = JSON.readValue(body, new TypeReference<>() {});
    long showtimeId = requiredId(values.get("showtimeId"), "showtimeId");
    Object rawSeats = values.get("seatIds");
    if (!(rawSeats instanceof List<?> seatValues) || seatValues.isEmpty()) {
      throw new ApiException(400, "Bad Request", "Debe seleccionar al menos un asiento");
    }
    List<Long> seatIds = new ArrayList<>();
    Set<Long> unique = new HashSet<>();
    for (Object value : seatValues) {
      long seatId = requiredId(value, "seatIds");
      if (!unique.add(seatId)) {
        throw new ApiException(400, "Bad Request", "La lista seatIds contiene valores duplicados");
      }
      seatIds.add(seatId);
    }
    return new ReservationRequest(showtimeId, seatIds);
  }

  private long requiredId(Object value, String name) {
    if (value == null) throw new ApiException(400, "Bad Request", "Falta el campo " + name);
    try {
      long id = new BigDecimal(value.toString()).longValueExact();
      if (id <= 0) throw new NumberFormatException();
      return id;
    } catch (NumberFormatException | ArithmeticException e) {
      throw new ApiException(400, "Bad Request", "El campo " + name + " debe ser un entero positivo");
    }
  }

  private String authenticatedEmail(Map<String,Object> event) {
    Object rawContext = event.get("requestContext");
    if (!(rawContext instanceof Map<?,?> requestContext)) return null;
    Object rawAuthorizer = requestContext.get("authorizer");
    if (!(rawAuthorizer instanceof Map<?,?> authorizer)) return null;
    Object rawJwt = authorizer.get("jwt");
    if (!(rawJwt instanceof Map<?,?> jwt)) return null;
    Object rawClaims = jwt.get("claims");
    if (!(rawClaims instanceof Map<?,?> claims)) return null;
    Object email = claims.get("email");
    return email == null ? null : email.toString();
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

  private record ReservationRequest(long showtimeId, List<Long> seatIds) {}
  private record Showtime(long roomId, BigDecimal basePrice) {}
  private static class ApiException extends RuntimeException {
    private final int status;
    private final String error;
    private ApiException(int status, String error, String message) {
      super(message);
      this.status = status;
      this.error = error;
    }
  }
}

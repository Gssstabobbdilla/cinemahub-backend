// Espejo de com.cinemahub.cinemahub.reservation.dto.* (backend)

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'EXPIRED' | 'CANCELLED';

// Los asientos NO viajan acá: se consultan aparte con
// ReservationService.findSeats -> GET /api/reservations/{id}/seats.
export interface Reservation {
  id: number;
  userId: number;
  status: ReservationStatus;
  expiresAt: string;
  createdAt: string;
}

export interface ReservationSeat {
  seatId: number;
  rowLabel: string;
  seatNumber: number;
  price: number;
}

// Espejo de ShowtimeSeatResponse (backend): TODOS los asientos de la sala de una función,
// marcando cuáles ya están tomados. Es lo que consume SeatMapComponent (seats + taken).
export interface ShowtimeSeat {
  seatId: number;
  rowLabel: string;
  seatNumber: number;
  seatType: string;
  taken: boolean;
}

// userId debería salir del usuario autenticado cuando haya login real; por ahora
// coincide con ReservationService.createReservation del backend.
export interface CreateReservationRequest {
  userId: number;
  showtimeId: number;
  seatIds: number[];
}
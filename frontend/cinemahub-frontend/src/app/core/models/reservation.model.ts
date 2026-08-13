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

// userId debería salir del usuario autenticado cuando haya login real; por ahora
// coincide con ReservationService.createReservation del backend.
export interface CreateReservationRequest {
  userId: number;
  showtimeId: number;
  seatIds: number[];
}
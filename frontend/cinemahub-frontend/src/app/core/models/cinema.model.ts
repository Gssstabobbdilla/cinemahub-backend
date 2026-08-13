// Espejo de com.cinemahub.cinemahub.cinema.dto.* (backend)

export interface Cinema {
  id: number;
  name: string;
  department: string | null;
  province: string | null;
  district: string | null;
  address: string | null;
  phone: string | null;
  latitude: number | null;
  longitude: number | null;
}

export interface CreateCinemaRequest {
  name: string;
}

export interface UpdateCinemaLocationRequest {
  department?: string;
  province?: string;
  district?: string;
  address?: string;
}

export interface Room {
  id: number;
  cinemaId: number;
  name: string;
  type: string;
  capacity: number;
}

// El cinemaId va por la URL (/cinemas/{cinemaId}/rooms), no en el body.
export interface CreateRoomRequest {
  name: string;
  capacity: number;
}

export interface Seat {
  id: number;
  roomId: number;
  rowLabel: string;
  seatNumber: number;
  seatType: string;
}

// El roomId va por la URL (/rooms/{roomId}/seats), no en el body.
export interface CreateSeatRequest {
  rowLabel: string;
  seatNumber: number;
}

export interface GenerateSeatsRequest {
  rowCount: number;
  seatsPerRow: number;
}
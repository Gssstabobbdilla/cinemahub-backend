// Espejo de com.cinemahub.cinemahub.showtime.dto.* (backend)

export type ShowtimeStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';

// Incluye movieTitle/roomName/cinemaName ya resueltos por el backend (evita que el
// frontend tenga que pedirlos aparte para armar una cartelera).
export interface Showtime {
  id: number;
  movieId: number;
  movieTitle: string;
  roomId: number;
  roomName: string;
  cinemaName: string;
  showDate: string;
  startTime: string;
  endTime: string;
  language: string;
  format: string;
  basePrice: number;
  status: ShowtimeStatus;
}

export interface CreateShowtimeRequest {
  movieId: number;
  roomId: number;
  showDate: string;
  startTime: string;
  endTime: string;
  basePrice: number;
}
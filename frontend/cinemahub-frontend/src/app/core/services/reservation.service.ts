import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateReservationRequest, Reservation, ReservationSeat } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  // El backend responde 409 si algún asiento ya está tomado (ver DuplicateResourceException) —
  // el componente que llame a esto debe manejar ese caso explícitamente.
  create(request: CreateReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.apiUrl}/reservations`, request);
  }

  findById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.apiUrl}/reservations/${id}`);
  }

  findSeats(id: number): Observable<ReservationSeat[]> {
    return this.http.get<ReservationSeat[]>(`${this.apiUrl}/reservations/${id}/seats`);
  }

  findByUser(userId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/users/${userId}/reservations`);
  }

  confirm(id: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.apiUrl}/reservations/${id}/confirm`, {});
  }

  cancel(id: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.apiUrl}/reservations/${id}/cancel`, {});
  }
}
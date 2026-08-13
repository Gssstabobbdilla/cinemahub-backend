import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateSeatRequest, GenerateSeatsRequest, Seat } from '../models/cinema.model';

@Injectable({ providedIn: 'root' })
export class SeatService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  findByRoom(roomId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.apiUrl}/rooms/${roomId}/seats`);
  }

  create(roomId: number, request: CreateSeatRequest): Observable<Seat> {
    return this.http.post<Seat>(`${this.apiUrl}/rooms/${roomId}/seats`, request);
  }

  generateSeats(roomId: number, request: GenerateSeatsRequest): Observable<Seat[]> {
    return this.http.post<Seat[]>(`${this.apiUrl}/rooms/${roomId}/seats/generate`, request);
  }

  findById(id: number): Observable<Seat> {
    return this.http.get<Seat>(`${this.apiUrl}/seats/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/seats/${id}`);
  }
}
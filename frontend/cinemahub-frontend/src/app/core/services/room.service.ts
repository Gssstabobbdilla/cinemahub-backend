import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateRoomRequest, Room } from '../models/cinema.model';

@Injectable({ providedIn: 'root' })
export class RoomService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  findByCinema(cinemaId: number): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.apiUrl}/cinemas/${cinemaId}/rooms`);
  }

  create(cinemaId: number, request: CreateRoomRequest): Observable<Room> {
    return this.http.post<Room>(`${this.apiUrl}/cinemas/${cinemaId}/rooms`, request);
  }

  findById(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/rooms/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rooms/${id}`);
  }
}
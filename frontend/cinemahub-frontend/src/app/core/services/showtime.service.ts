import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateShowtimeRequest, Showtime } from '../models/showtime.model';

@Injectable({ providedIn: 'root' })
export class ShowtimeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/showtimes`;

  // Requiere movieId, o roomId+date (igual que el backend: no hay un findAll sin filtros).
  search(filters: { movieId?: number; roomId?: number; date?: string }): Observable<Showtime[]> {
    let params = new HttpParams();
    if (filters.movieId != null) {
      params = params.set('movieId', filters.movieId);
    }
    if (filters.roomId != null) {
      params = params.set('roomId', filters.roomId);
    }
    if (filters.date) {
      params = params.set('date', filters.date);
    }
    return this.http.get<Showtime[]>(this.baseUrl, { params });
  }

  findById(id: number): Observable<Showtime> {
    return this.http.get<Showtime>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateShowtimeRequest): Observable<Showtime> {
    return this.http.post<Showtime>(this.baseUrl, request);
  }

  cancel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
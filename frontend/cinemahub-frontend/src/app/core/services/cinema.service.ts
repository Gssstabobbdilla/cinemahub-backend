import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Cinema, CreateCinemaRequest, UpdateCinemaLocationRequest } from '../models/cinema.model';

@Injectable({ providedIn: 'root' })
export class CinemaService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/cinemas`;

  findAll(): Observable<Cinema[]> {
    return this.http.get<Cinema[]>(this.baseUrl);
  }

  findById(id: number): Observable<Cinema> {
    return this.http.get<Cinema>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateCinemaRequest): Observable<Cinema> {
    return this.http.post<Cinema>(this.baseUrl, request);
  }

  updateLocation(id: number, request: UpdateCinemaLocationRequest): Observable<Cinema> {
    return this.http.put<Cinema>(`${this.baseUrl}/${id}/location`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
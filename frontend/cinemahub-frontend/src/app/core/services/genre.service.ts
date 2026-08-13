import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Genre, GenreRequest } from '../models/movie.model';

@Injectable({ providedIn: 'root' })
export class GenreService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/genres`;

  findAll(): Observable<Genre[]> {
    return this.http.get<Genre[]>(this.baseUrl);
  }

  findById(id: number): Observable<Genre> {
    return this.http.get<Genre>(`${this.baseUrl}/${id}`);
  }

  create(request: GenreRequest): Observable<Genre> {
    return this.http.post<Genre>(this.baseUrl, request);
  }

  update(id: number, request: GenreRequest): Observable<Genre> {
    return this.http.put<Genre>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
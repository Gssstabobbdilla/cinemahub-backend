import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AddGenreRequest, ChangeMovieStatusRequest, CreateMovieRequest, Genre, Movie, MovieStatus, UpdateMovieRequest } from '../models/movie.model';

@Injectable({ providedIn: 'root' })
export class MovieService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/movies`;
  
  // Sin status trae todas las películas; con status filtra (ACTIVE, COMING_SOON, etc.).
  findAll(status?: MovieStatus): Observable<Movie[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<Movie[]>(this.baseUrl, { params });
  }

  findById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateMovieRequest): Observable<Movie> {
    return this.http.post<Movie>(this.baseUrl, request);
  }

  changeStatus(id: number, request: ChangeMovieStatusRequest): Observable<Movie> {
    return this.http.patch<Movie>(`${this.baseUrl}/${id}/status`, request);
  }

  findGenres(id: number): Observable<Genre[]> {
    return this.http.get<Genre[]>(`${this.baseUrl}/${id}/genres`);
  }

  addGenre(id: number, request: AddGenreRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/genres`, request);
  }

  removeGenre(id: number, genreId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}/genres/${genreId}`);
  }

  update(id: number, request: UpdateMovieRequest): Observable<Movie> {
    return this.http.put<Movie>(`${this.baseUrl}/${id}`, request);
  }
}


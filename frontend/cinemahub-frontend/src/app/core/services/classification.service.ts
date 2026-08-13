import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Classification, ClassificationRequest } from '../models/movie.model';

@Injectable({ providedIn: 'root' })
export class ClassificationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/classifications`;

  findAll(): Observable<Classification[]> {
    return this.http.get<Classification[]>(this.baseUrl);
  }

  findById(id: number): Observable<Classification> {
    return this.http.get<Classification>(`${this.baseUrl}/${id}`);
  }

  create(request: ClassificationRequest): Observable<Classification> {
    return this.http.post<Classification>(this.baseUrl, request);
  }

  update(id: number, request: ClassificationRequest): Observable<Classification> {
    return this.http.put<Classification>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
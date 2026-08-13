import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Permission, PermissionRequest } from '../models/security.model';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/permissions`;

  findAll(): Observable<Permission[]> {
    return this.http.get<Permission[]>(this.baseUrl);
  }

  findById(id: number): Observable<Permission> {
    return this.http.get<Permission>(`${this.baseUrl}/${id}`);
  }

  create(request: PermissionRequest): Observable<Permission> {
    return this.http.post<Permission>(this.baseUrl, request);
  }

  update(id: number, request: PermissionRequest): Observable<Permission> {
    return this.http.put<Permission>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
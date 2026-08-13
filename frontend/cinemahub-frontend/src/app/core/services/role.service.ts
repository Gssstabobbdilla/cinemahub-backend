import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AssignPermissionRequest, Permission, Role, RoleRequest } from '../models/security.model';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/roles`;

  findAll(): Observable<Role[]> {
    return this.http.get<Role[]>(this.baseUrl);
  }

  findById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.baseUrl}/${id}`);
  }

  create(request: RoleRequest): Observable<Role> {
    return this.http.post<Role>(this.baseUrl, request);
  }

  update(id: number, request: RoleRequest): Observable<Role> {
    return this.http.put<Role>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  findPermissions(id: number): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.baseUrl}/${id}/permissions`);
  }

  assignPermission(id: number, request: AssignPermissionRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/permissions`, request);
  }

  removePermission(id: number, permissionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}/permissions/${permissionId}`);
  }
}
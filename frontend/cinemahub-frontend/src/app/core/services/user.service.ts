import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AssignRoleRequest,
  ChangeUserStatusRequest,
  RegisterUserRequest,
  Role,
  UpdateProfileRequest,
  User
} from '../models/security.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/users`;

  findAll(): Observable<User[]> {
    return this.http.get<User[]>(this.baseUrl);
  }

  findById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  register(request: RegisterUserRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, request);
  }

  updateProfile(id: number, request: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}/profile`, request);
  }

  changeStatus(id: number, request: ChangeUserStatusRequest): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}/status`, request);
  }

  findRoles(id: number): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.baseUrl}/${id}/roles`);
  }

  assignRole(id: number, request: AssignRoleRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/roles`, request);
  }

  removeRole(id: number, roleId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}/roles/${roleId}`);
  }
}
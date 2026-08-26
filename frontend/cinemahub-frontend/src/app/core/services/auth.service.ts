import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest } from '../models/auth.model';
import { RegisterUserRequest } from '../models/security.model';
import { CurrentUserService } from './current-user.service';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private tokenStorage = inject(TokenStorageService);
  private currentUser = inject(CurrentUserService);
  private apiUrl = environment.apiUrl;

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/login`, request)
      .pipe(tap(response => this.onAuthenticated(response)));
  }

  register(request: RegisterUserRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/register`, request)
      .pipe(tap(response => this.onAuthenticated(response)));
  }

  logout(): void {
    this.tokenStorage.clearToken();
    this.currentUser.clear();
  }

  private onAuthenticated(response: AuthResponse): void {
    this.tokenStorage.setToken(response.token);
    this.currentUser.setSession({
      userId: response.userId,
      firstName: response.firstName,
      lastName: response.lastName,
      email: response.email,
      roles: response.roles
    });
  }
}
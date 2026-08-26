import { Injectable, computed, inject, signal } from '@angular/core';

import { TokenStorageService } from './token-storage.service';

export interface CurrentUserSession {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
}

interface JwtPayload {
  sub: string;
  userId: number;
  firstName: string;
  lastName: string;
  roles: string[];
  exp: number;
}

// Fuente única de verdad sobre "quién está logueado", derivada del JWT guardado en
// TokenStorageService. No valida la firma (eso lo hace el backend en cada request);
// solo lee los claims para poder mostrar la UI sin llamadas extra al arrancar la app.
@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private tokenStorage = inject(TokenStorageService);

  private session = signal<CurrentUserSession | null>(this.restoreFromToken());

  readonly userId = computed(() => this.session()?.userId ?? null);
  readonly fullName = computed(() => {
    const s = this.session();
    return s ? `${s.firstName} ${s.lastName}` : null;
  });
  readonly roles = computed(() => this.session()?.roles ?? []);
  readonly isAuthenticated = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.roles().includes('ROLE_ADMIN'));

  setSession(session: CurrentUserSession): void {
    this.session.set(session);
  }

  clear(): void {
    this.session.set(null);
  }

  private restoreFromToken(): CurrentUserSession | null {
    const token = this.tokenStorage.getToken();
    if (!token) {
      return null;
    }

    const payload = this.decode(token);
    if (!payload || payload.exp * 1000 < Date.now()) {
      this.tokenStorage.clearToken();
      return null;
    }

    return {
      userId: payload.userId,
      firstName: payload.firstName,
      lastName: payload.lastName,
      email: payload.sub,
      roles: payload.roles ?? []
    };
  }

  private decode(token: string): JwtPayload | null {
    try {
      const payload = token.split('.')[1];
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(json);
    } catch {
      return null;
    }
  }
}
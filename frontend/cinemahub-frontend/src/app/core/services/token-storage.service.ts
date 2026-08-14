import { Injectable } from '@angular/core';

const TOKEN_KEY = 'cinemahub_token';

// Envuelve el acceso a localStorage para que, si más adelante cambiamos la estrategia
// de almacenamiento del token (por ejemplo a una cookie httpOnly manejada por el backend),
// solo haya que tocar este archivo — nada más en la app conoce el mecanismo de storage.
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }
}
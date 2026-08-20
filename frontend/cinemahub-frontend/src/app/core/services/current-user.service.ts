import { Injectable, signal } from '@angular/core';

// Reemplaza esto por el usuario autenticado real cuando exista login (authInterceptor y
// TokenStorageService ya están listos para ese momento). Mientras tanto, es un valor que
// el usuario ingresa a mano y que comparten reservas/cuenta, para no repetir el mismo
// input "¿quién sos?" en cada feature.
@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  readonly userId = signal<number | null>(null);

  setUserId(id: number | null): void {
    this.userId.set(id);
  }
}
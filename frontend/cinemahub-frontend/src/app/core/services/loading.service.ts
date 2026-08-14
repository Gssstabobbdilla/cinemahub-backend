import { Injectable, computed, signal } from '@angular/core';

// Cuenta requests activos en vez de un simple booleano: si dos llamadas HTTP están en
// vuelo al mismo tiempo, no queremos que la primera en terminar apague el spinner
// mientras la segunda sigue esperando.
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly activeRequests = signal(0);

  readonly loading = computed(() => this.activeRequests() > 0);

  show(): void {
    this.activeRequests.update(count => count + 1);
  }

  hide(): void {
    this.activeRequests.update(count => Math.max(0, count - 1));
  }
}
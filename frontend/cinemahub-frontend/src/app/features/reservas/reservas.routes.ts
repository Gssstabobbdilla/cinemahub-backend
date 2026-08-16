import { Routes } from '@angular/router';

// Se va a expandir cuando construyamos la feature de verdad, por ejemplo:
// { path: ':showtimeId', loadComponent: () => import('./seat-selection-page.component')... }
export const RESERVAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./reservas-page.component').then(m => m.ReservasPageComponent)
  }
];
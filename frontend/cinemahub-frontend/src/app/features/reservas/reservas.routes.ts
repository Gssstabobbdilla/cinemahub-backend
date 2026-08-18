import { Routes } from '@angular/router';

// Siempre se llega acá desde un link con showtimeId (ver MovieDetailPageComponent en la
// feature cartelera) — no hay una vista de "reservas" sin función elegida todavía.
export const RESERVAS_ROUTES: Routes = [
  {
    path: ':showtimeId',
    loadComponent: () => import('./reservas-page.component').then(m => m.ReservasPageComponent)
  }
];
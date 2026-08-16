import { Routes } from '@angular/router';

// Se va a expandir con sub-rutas cuando construyamos la feature de verdad, por ejemplo:
// { path: ':movieId', loadComponent: () => import('./movie-detail-page.component')... }
export const CARTELERA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./cartelera-page.component').then(m => m.CarteleraPageComponent)
  }
];
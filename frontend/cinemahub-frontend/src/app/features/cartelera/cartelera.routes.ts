import { Routes } from '@angular/router';

export const CARTELERA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./cartelera-page.component').then(m => m.CarteleraPageComponent)
  },
  {
    path: ':movieId',
    loadComponent: () => import('./movie-detail-page.component').then(m => m.MovieDetailPageComponent)
  }
];
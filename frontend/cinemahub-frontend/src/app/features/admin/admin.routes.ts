import { Routes } from '@angular/router';

// Se va a expandir con sub-rutas por dominio cuando construyamos la feature de verdad,
// por ejemplo: 'peliculas', 'cines', 'salas', 'funciones', 'productos', 'promociones'.
export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./admin-page.component').then(m => m.AdminPageComponent)
  }
];
import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./admin-page.component').then(m => m.AdminPageComponent),
    children: [
      { path: '', redirectTo: 'productos', pathMatch: 'full' },
      {
        path: 'productos',
        loadComponent: () => import('../productos/productos-page.component').then(m => m.ProductosPageComponent)

      },
      {
        path: 'peliculas',
        loadComponent: () => import('../peliculas/peliculas-page.component').then(m => m.PeliculasPageComponent)
      },
      {
        path: 'cines',
        loadComponent: () => import('../cines/cines-page.component').then(m => m.CinesPageComponent)
      }
      // próximos: 'peliculas', 'cines', 'funciones', 'promociones' — mismo patrón
    ]
  }
];
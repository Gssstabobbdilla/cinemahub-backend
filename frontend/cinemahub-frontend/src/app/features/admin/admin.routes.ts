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
      }
      // próximos: 'peliculas', 'cines', 'funciones', 'promociones' — mismo patrón
    ]
  }
];
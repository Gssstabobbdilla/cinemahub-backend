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
      },
      {
        path: 'funciones',
        loadComponent: () => import('../funciones/funciones-page.component').then(m => m.FuncionesPageComponent)
      },
      {
        path: 'promociones',
        loadComponent: () => import('../promociones/promociones-page.component').then(m => m.PromocionesPageComponent)
      },
      {
        path: 'pagos',
        loadComponent: () => import('../pagos/pagos-page.component').then(m => m.PagosPageComponent)
      },
      {
        path: 'usuarios',
        loadComponent: () => import('../usuarios/usuarios-page.component').then(m => m.UsuariosPageComponent)
      }

    ]
  }
];
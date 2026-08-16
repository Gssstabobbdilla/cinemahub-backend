import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'cartelera', pathMatch: 'full' },
  {
    path: 'cartelera',
    loadChildren: () => import('./features/cartelera/cartelera.routes').then(m => m.CARTELERA_ROUTES)
  },
  {
    path: 'reservas',
    loadChildren: () => import('./features/reservas/reservas.routes').then(m => m.RESERVAS_ROUTES)
  },
  {
    path: 'checkout',
    loadChildren: () => import('./features/checkout/checkout.routes').then(m => m.CHECKOUT_ROUTES)
  },
  {
    path: 'cuenta',
    loadChildren: () => import('./features/cuenta/cuenta.routes').then(m => m.CUENTA_ROUTES)
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  { path: '**', redirectTo: 'cartelera' }
];
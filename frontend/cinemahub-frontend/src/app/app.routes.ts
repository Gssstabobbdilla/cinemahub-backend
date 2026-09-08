import { Routes } from '@angular/router';

import { adminGuard, authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'cartelera', pathMatch: 'full' },
  {
    path: 'cartelera',
    loadChildren: () => import('./features/cartelera/cartelera.routes').then(m => m.CARTELERA_ROUTES)
  },
  {
    path: 'login',
    data: { hideNav: true },
    loadChildren: () => import('./features/login/login.routes').then(m => m.LOGIN_ROUTES)
},
  {
    path: 'reservas',
    canActivate: [authGuard],
    loadChildren: () => import('./features/reservas/reservas.routes').then(m => m.RESERVAS_ROUTES)
  },
  {
    path: 'checkout',
    canActivate: [authGuard],
    loadChildren: () => import('./features/checkout/checkout.routes').then(m => m.CHECKOUT_ROUTES)
  },
  {
    path: 'cuenta',
    canActivate: [authGuard],
    loadChildren: () => import('./features/cuenta/cuenta.routes').then(m => m.CUENTA_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  { path: '**', redirectTo: 'cartelera' }
];
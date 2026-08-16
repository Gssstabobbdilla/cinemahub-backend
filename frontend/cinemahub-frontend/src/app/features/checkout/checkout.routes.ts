import { Routes } from '@angular/router';

// Se va a expandir cuando construyamos la feature de verdad, por ejemplo:
// { path: ':orderId', loadComponent: () => import('./checkout-page.component')... }
export const CHECKOUT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./checkout-page.component').then(m => m.CheckoutPageComponent)
  }
];
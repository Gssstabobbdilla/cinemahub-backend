import { Routes } from '@angular/router';

// Se va a expandir con sub-rutas cuando construyamos la feature de verdad, por ejemplo:
// 'reservas' (historial), 'membresia', 'notificaciones'.
export const CUENTA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./cuenta-page.component').then(m => m.CuentaPageComponent)
  }
];
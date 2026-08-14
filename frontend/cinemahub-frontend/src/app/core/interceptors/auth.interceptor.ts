import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { TokenStorageService } from '../services/token-storage.service';

// El backend todavía no valida JWT (UserService.register guarda el password sin encriptar,
// con un TODO pendiente de Spring Security — ver conversación previa del backend). Este
// interceptor queda listo para cuando eso exista: hoy, si no hay token guardado, no agrega
// el header y la request sigue exactamente igual que antes.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const token = tokenStorage.getToken();

  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  });
  return next(authReq);
};
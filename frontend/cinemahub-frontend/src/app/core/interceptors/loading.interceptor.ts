import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';

import { LoadingService } from '../services/loading.service';

// Un componente raíz (por ejemplo app.component.html) puede leer LoadingService.loading()
// para mostrar/ocultar un spinner global sin que cada componente feature tenga que
// manejar su propio estado de "cargando".
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  loadingService.show();

  return next(req).pipe(
    finalize(() => loadingService.hide())
  );
};
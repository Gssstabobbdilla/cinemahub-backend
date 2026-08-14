import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

// Espejo, del lado del cliente, de com.cinemahub.cinemahub.common.web.GlobalExceptionHandler
// del backend. Ese handler siempre devuelve un body con esta forma:
//   { timestamp, status, error, message }                 (404, 409, 400 genérico)
//   { timestamp, status, error: "Validation Failed", fields: { campo: mensaje } }  (400 de @Valid)
export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message?: string;
  fields?: Record<string, string>;
}

// Forma normalizada que consumen los componentes: siempre un mensaje humano-legible,
// más el detalle campo -> mensaje cuando el error viene de una validación.
export interface AppError {
  status: number;
  message: string;
  fields?: Record<string, string>;
}

function toAppError(httpError: HttpErrorResponse): AppError {
  const body = httpError.error as ApiErrorBody | null;

  if (body?.fields) {
    return {
      status: httpError.status,
      message: 'Hay campos inválidos en el formulario.',
      fields: body.fields
    };
  }

  if (body?.message) {
    return { status: httpError.status, message: body.message };
  }

  // El backend no respondió con el formato esperado (caída de red, 502 del load balancer, etc.).
  return { status: httpError.status, message: 'Ocurrió un error de conexión con el servidor.' };
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((httpError: HttpErrorResponse) => throwError(() => toAppError(httpError)))
  );
};
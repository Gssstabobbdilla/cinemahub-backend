import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AppError, errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('convierte un error simple (404 con message) en AppError', () => {
    let captured: AppError | undefined;
    http.get('/api/roles/99').subscribe({ error: (err: AppError) => (captured = err) });

    httpMock.expectOne('/api/roles/99').flush(
      { timestamp: 'x', status: 404, error: 'Not Found', message: 'Role no encontrado: id=99' },
      { status: 404, statusText: 'Not Found' }
    );

    expect(captured?.status).toBe(404);
    expect(captured?.message).toBe('Role no encontrado: id=99');
    expect(captured?.fields).toBeUndefined();
  });

  it('convierte un error de validación (400 con fields) preservando el detalle por campo', () => {
    let captured: AppError | undefined;
    http.post('/api/roles', {}).subscribe({ error: (err: AppError) => (captured = err) });

    httpMock.expectOne('/api/roles').flush(
      { timestamp: 'x', status: 400, error: 'Validation Failed', fields: { name: 'no debe estar vacío' } },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(captured?.fields).toEqual({ name: 'no debe estar vacío' });
    expect(captured?.message).toContain('inválidos');
  });

  it('cae a un mensaje genérico cuando el body no tiene la forma esperada', () => {
    let captured: AppError | undefined;
    http.get('/api/roles').subscribe({ error: (err: AppError) => (captured = err) });

    httpMock.expectOne('/api/roles').flush('<html>502 Bad Gateway</html>', {
      status: 502,
      statusText: 'Bad Gateway'
    });

    expect(captured?.status).toBe(502);
    expect(captured?.message).toBe('Ocurrió un error de conexión con el servidor.');
  });
});
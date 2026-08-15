import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { authInterceptor } from './auth.interceptor';
import { TokenStorageService } from '../services/token-storage.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let tokenStorage: TokenStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
    tokenStorage.clearToken();
  });

  afterEach(() => {
    httpMock.verify();
    tokenStorage.clearToken();
  });

  it('no agrega el header Authorization cuando no hay token guardado', () => {
    http.get('/api/roles').subscribe();
    const req = httpMock.expectOne('/api/roles');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('agrega el header Authorization con Bearer cuando hay un token guardado', () => {
    tokenStorage.setToken('fake-jwt-token');

    http.get('/api/roles').subscribe();
    const req = httpMock.expectOne('/api/roles');
    expect(req.request.headers.get('Authorization')).toBe('Bearer fake-jwt-token');
    req.flush([]);
  });
});
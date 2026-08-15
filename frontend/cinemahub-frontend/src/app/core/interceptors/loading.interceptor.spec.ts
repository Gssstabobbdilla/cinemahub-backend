import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { loadingInterceptor } from './loading.interceptor';
import { LoadingService } from '../services/loading.service';

describe('loadingInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let loadingService: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([loadingInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    loadingService = TestBed.inject(LoadingService);
  });

  afterEach(() => httpMock.verify());

  it('pone loading en true mientras la request está en vuelo, y en false al terminar', () => {
    expect(loadingService.loading()).toBe(false);

    http.get('/api/roles').subscribe();
    expect(loadingService.loading()).toBe(true);

    httpMock.expectOne('/api/roles').flush([]);
    expect(loadingService.loading()).toBe(false);
  });

  it('se mantiene en true mientras al menos una de dos requests concurrentes sigue en vuelo', () => {
    http.get('/api/roles').subscribe();
    http.get('/api/permissions').subscribe();
    expect(loadingService.loading()).toBe(true);

    httpMock.expectOne('/api/roles').flush([]);
    expect(loadingService.loading()).toBe(true); // la segunda todavía está en vuelo

    httpMock.expectOne('/api/permissions').flush([]);
    expect(loadingService.loading()).toBe(false);
  });

  it('vuelve a false incluso cuando la request termina en error', () => {
    http.get('/api/roles').subscribe({ error: () => {} });
    expect(loadingService.loading()).toBe(true);

    httpMock.expectOne('/api/roles').flush('error', { status: 500, statusText: 'Server Error' });
    expect(loadingService.loading()).toBe(false);
  });
});
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Classification, ClassificationRequest } from '../models/movie.model';
import { ClassificationService } from './classification.service';

describe('ClassificationService', () => {
  let service: ClassificationService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/classifications`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ClassificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /classifications', () => {
    const mock: Classification[] = [{ id: 1, code: 'PG-13', description: null }];
    service.findAll().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('findById hace GET /classifications/:id', () => {
    const mock: Classification = { id: 1, code: 'PG-13', description: null };
    service.findById(1).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('create hace POST /classifications con el body correcto', () => {
    const request: ClassificationRequest = { code: 'PG-13', description: 'Supervisión' };
    const mock: Classification = { id: 1, ...request, description: request.description ?? null};
    service.create(request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('update hace PUT /classifications/:id con el body correcto', () => {
    const request: ClassificationRequest = { code: 'R', description: 'Restringido' };
    const mock: Classification = { id: 1, ...request, description: request.description ?? null };
    service.update(1, request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('delete hace DELETE /classifications/:id', () => {
    service.delete(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
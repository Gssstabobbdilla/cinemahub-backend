import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Permission, PermissionRequest } from '../models/security.model';
import { PermissionService } from './permission.service';

describe('PermissionService', () => {
  let service: PermissionService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/permissions`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PermissionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /permissions', () => {
    const mock: Permission[] = [{ id: 1, name: 'MOVIE_WRITE', description: null }];
    service.findAll().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('findById hace GET /permissions/:id', () => {
    const mock: Permission = { id: 1, name: 'MOVIE_WRITE', description: null };
    service.findById(1).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('create hace POST /permissions con el body correcto', () => {
    const request: PermissionRequest = { name: 'MOVIE_WRITE', description: 'desc' };
    const mock: Permission = { id: 1, ...request, description: request.description ?? null };
    service.create(request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('update hace PUT /permissions/:id con el body correcto', () => {
    const request: PermissionRequest = { name: 'MOVIE_READ', description: 'desc2' };
    const mock: Permission = { id: 1, ...request, description: request.description ?? null };
    service.update(1, request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('delete hace DELETE /permissions/:id', () => {
    service.delete(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
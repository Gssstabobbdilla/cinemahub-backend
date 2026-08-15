import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Permission, Role, RoleRequest } from '../models/security.model';
import { RoleService } from './role.service';

describe('RoleService', () => {
  let service: RoleService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/roles`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(RoleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /roles', () => {
    const mock: Role[] = [
      {
        id: 1,
        name: 'ADMIN',
        description: null
      }
    ];

    service.findAll().subscribe(res => expect(res).toEqual(mock));

    const req = httpMock.expectOne(baseUrl);

    expect(req.request.method).toBe('GET');

    req.flush(mock);
  });

  it('findById hace GET /roles/:id', () => {
    const mock: Role = {
      id: 1,
      name: 'ADMIN',
      description: null
    };

    service.findById(1).subscribe(res => expect(res).toEqual(mock));

    const req = httpMock.expectOne(`${baseUrl}/1`);

    expect(req.request.method).toBe('GET');

    req.flush(mock);
  });

  it('create hace POST /roles con el body correcto', () => {
    const request: RoleRequest = {
      name: 'ADMIN',
      description: 'desc'
    };

    const mock: Role = {
      id: 1,
      ...request,
      description: request.description ?? null
    };

    service.create(request).subscribe(res => expect(res).toEqual(mock));

    const req = httpMock.expectOne(baseUrl);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);

    req.flush(mock);
  });

  it('update hace PUT /roles/:id con el body correcto', () => {
    const request: RoleRequest = {
      name: 'ADMIN2',
      description: 'desc2'
    };

    const mock: Role = {
      id: 1,
      ...request,
      description: request.description ?? null
    };

    service.update(1, request).subscribe(res => expect(res).toEqual(mock));

    const req = httpMock.expectOne(`${baseUrl}/1`);

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);

    req.flush(mock);
  });

  it('delete hace DELETE /roles/:id', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/1`);

    expect(req.request.method).toBe('DELETE');

    req.flush(null);
  });

  it('findPermissions hace GET /roles/:id/permissions', () => {
    const mock: Permission[] = [
      {
        id: 2,
        name: 'MOVIE_WRITE',
        description: null
      }
    ];

    service.findPermissions(1).subscribe(res => expect(res).toEqual(mock));

    const req = httpMock.expectOne(`${baseUrl}/1/permissions`);

    expect(req.request.method).toBe('GET');

    req.flush(mock);
  });

  it('assignPermission hace POST /roles/:id/permissions con el body correcto', () => {
    service.assignPermission(1, { permissionId: 2 }).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/1/permissions`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ permissionId: 2 });

    req.flush(null);
  });

  it('removePermission hace DELETE /roles/:id/permissions/:permissionId', () => {
    service.removePermission(1, 2).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/1/permissions/2`);

    expect(req.request.method).toBe('DELETE');

    req.flush(null);
  });
});
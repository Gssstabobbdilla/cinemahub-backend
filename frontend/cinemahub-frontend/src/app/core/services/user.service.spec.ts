import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { RegisterUserRequest, Role, User } from '../models/security.model';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/users`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /users', () => {
    const mock: User[] = [];
    service.findAll().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('findById hace GET /users/:id', () => {
    const mock = { id: 1 } as User;
    service.findById(1).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('register hace POST /users/register con el body correcto', () => {
    const request: RegisterUserRequest = {
      firstName: 'Ana',
      lastName: 'Test',
      email: 'ana@cinemahub.local',
      password: 'clave12345'
    };
    const mock = { id: 1 } as User;
    service.register(request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('updateProfile hace PUT /users/:id/profile con el body correcto', () => {
    const request = { firstName: 'Ana', lastName: 'Test', phone: '999999999' };
    const mock = { id: 1 } as User;
    service.updateProfile(1, request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1/profile`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('changeStatus hace PATCH /users/:id/status con el body correcto', () => {
    const mock = { id: 1 } as User;
    service.changeStatus(1, { status: 'BLOCKED' }).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'BLOCKED' });
    req.flush(mock);
  });

  it('findRoles hace GET /users/:id/roles', () => {
    const mock: Role[] = [{ id: 2, name: 'ADMIN', description: null }];
    service.findRoles(1).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1/roles`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('assignRole hace POST /users/:id/roles con el body correcto', () => {
    service.assignRole(1, { roleId: 2 }).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/roles`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ roleId: 2 });
    req.flush(null);
  });

  it('removeRole hace DELETE /users/:id/roles/:roleId', () => {
    service.removeRole(1, 2).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/roles/2`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
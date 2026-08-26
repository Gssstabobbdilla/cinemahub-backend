import { TestBed } from '@angular/core/testing';

import { TokenStorageService } from './token-storage.service';
import { CurrentUserService } from './current-user.service';

// Genera un JWT falso (header.payload.signature) con el mismo shape de claims que
// emite el backend real. La firma no se valida en el cliente — solo se decodifica.
function buildToken(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.fake-signature`;
}

describe('CurrentUserService', () => {
  let tokenStorage: TokenStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    tokenStorage = TestBed.inject(TokenStorageService);
    tokenStorage.clearToken();
  });

  afterEach(() => tokenStorage.clearToken());

  it('sin token guardado: no hay sesión', () => {
    const service = TestBed.inject(CurrentUserService);

    expect(service.isAuthenticated()).toBe(false);
    expect(service.userId()).toBeNull();
    expect(service.fullName()).toBeNull();
    expect(service.roles()).toEqual([]);
  });

  it('con un token válido: restaura la sesión desde sus claims', () => {
    const token = buildToken({
      sub: 'ana@cinemahub.local',
      userId: 1,
      firstName: 'Ana',
      lastName: 'Test',
      roles: ['ROLE_USER'],
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    tokenStorage.setToken(token);

    const service = TestBed.inject(CurrentUserService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.userId()).toBe(1);
    expect(service.fullName()).toBe('Ana Test');
    expect(service.roles()).toEqual(['ROLE_USER']);
    expect(service.isAdmin()).toBe(false);
  });

  it('isAdmin es true solo si los roles incluyen ROLE_ADMIN', () => {
    const token = buildToken({
      sub: 'admin@cinemahub.local',
      userId: 2,
      firstName: 'Val',
      lastName: 'Admin',
      roles: ['ROLE_USER', 'ROLE_ADMIN'],
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    tokenStorage.setToken(token);

    const service = TestBed.inject(CurrentUserService);

    expect(service.isAdmin()).toBe(true);
  });

  it('con un token expirado: limpia el storage y no restaura sesión', () => {
    const token = buildToken({
      sub: 'ana@cinemahub.local',
      userId: 1,
      firstName: 'Ana',
      lastName: 'Test',
      roles: ['ROLE_USER'],
      exp: Math.floor(Date.now() / 1000) - 3600 // expiró hace 1 hora
    });
    tokenStorage.setToken(token);

    const service = TestBed.inject(CurrentUserService);

    expect(service.isAuthenticated()).toBe(false);
    expect(tokenStorage.getToken()).toBeNull();
  });

  it('con un token corrupto: no rompe, simplemente no hay sesión', () => {
    tokenStorage.setToken('esto-no-es-un-jwt-valido');

    const service = TestBed.inject(CurrentUserService);

    expect(service.isAuthenticated()).toBe(false);
  });

  it('setSession actualiza userId, fullName, roles e isAuthenticated', () => {
    const service = TestBed.inject(CurrentUserService);

    service.setSession({
      userId: 5,
      firstName: 'Beto',
      lastName: 'Reyes',
      email: 'beto@cinemahub.local',
      roles: ['ROLE_USER']
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.userId()).toBe(5);
    expect(service.fullName()).toBe('Beto Reyes');
  });

  it('clear() borra la sesión activa', () => {
    const service = TestBed.inject(CurrentUserService);

    service.setSession({
      userId: 5,
      firstName: 'Beto',
      lastName: 'Reyes',
      email: 'beto@cinemahub.local',
      roles: ['ROLE_USER']
    });
    service.clear();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.userId()).toBeNull();
  });
});
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';

import { CurrentUserService } from '../services/current-user.service';
import { adminGuard, authGuard } from './auth.guard';

describe('authGuard / adminGuard', () => {
  let currentUserServiceSpy: {
    isAuthenticated: ReturnType<typeof vi.fn>;
    isAdmin: ReturnType<typeof vi.fn>;
  };
  let routerSpy: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    currentUserServiceSpy = {
      isAuthenticated: vi.fn(),
      isAdmin: vi.fn()
    };
    routerSpy = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: CurrentUserService, useValue: currentUserServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  it('authGuard permite el paso si hay sesión iniciada', () => {
    currentUserServiceSpy.isAuthenticated.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );

    expect(result).toBe(true);
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('authGuard redirige a /login si no hay sesión', () => {
    currentUserServiceSpy.isAuthenticated.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );

    expect(result).toBe(false);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('adminGuard permite el paso si hay sesión Y es admin', () => {
    currentUserServiceSpy.isAuthenticated.mockReturnValue(true);
    currentUserServiceSpy.isAdmin.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any)
    );

    expect(result).toBe(true);
  });

  it('adminGuard redirige a /login si hay sesión pero NO es admin', () => {
    currentUserServiceSpy.isAuthenticated.mockReturnValue(true);
    currentUserServiceSpy.isAdmin.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any)
    );

    expect(result).toBe(false);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('adminGuard redirige a /login si no hay sesión', () => {
    currentUserServiceSpy.isAuthenticated.mockReturnValue(false);
    currentUserServiceSpy.isAdmin.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any)
    );

    expect(result).toBe(false);
  });
});
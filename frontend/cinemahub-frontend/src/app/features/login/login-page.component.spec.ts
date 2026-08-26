import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthResponse } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';

import { LoginPageComponent } from './login-page.component';

describe('LoginPageComponent', () => {
  let fixture: ComponentFixture<LoginPageComponent>;

  let authServiceSpy: {
    login: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
  };
  let routerSpy: { navigate: ReturnType<typeof vi.fn> };

  const authResponse: AuthResponse = {
    token: 'fake.jwt.token',
    userId: 1,
    firstName: 'Ana',
    lastName: 'Test',
    email: 'ana@cinemahub.local',
    roles: ['ROLE_USER']
  };

  beforeEach(() => {
    authServiceSpy = { login: vi.fn(), register: vi.fn() };
    routerSpy = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    fixture = TestBed.createComponent(LoginPageComponent);
  });

  it('arranca en modo login', () => {
    fixture.detectChanges();
    expect(fixture.componentInstance.mode()).toBe('login');
  });

  it('setMode cambia entre login y register, y limpia el error', () => {
    fixture.detectChanges();
    fixture.componentInstance.error.set('algo');

    fixture.componentInstance.setMode('register');

    expect(fixture.componentInstance.mode()).toBe('register');
    expect(fixture.componentInstance.error()).toBeNull();
  });

  it('submit en modo login valida que email y password estén completos', () => {
    fixture.detectChanges();
    fixture.componentInstance.email.set('');
    fixture.componentInstance.password.set('');

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error()).toBe('Completa email y contraseña.');
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('submit en modo login llama a authService.login y navega a /cuenta', () => {
    authServiceSpy.login.mockReturnValue(of(authResponse));

    fixture.detectChanges();
    fixture.componentInstance.email.set('ana@cinemahub.local');
    fixture.componentInstance.password.set('clave12345');

    fixture.componentInstance.submit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      email: 'ana@cinemahub.local',
      password: 'clave12345'
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/cuenta']);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('submit en modo login setea error() cuando las credenciales son inválidas', () => {
    authServiceSpy.login.mockReturnValue(
      throwError(() => ({ status: 401, message: 'Email o contraseña incorrectos' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.email.set('ana@cinemahub.local');
    fixture.componentInstance.password.set('mala-clave');

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error()).toBe('Email o contraseña incorrectos');
    expect(fixture.componentInstance.loading()).toBe(false);
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('submit en modo register valida nombre y apellido además de email/password', () => {
    fixture.detectChanges();
    fixture.componentInstance.setMode('register');
    fixture.componentInstance.email.set('ana@cinemahub.local');
    fixture.componentInstance.password.set('clave12345');
    fixture.componentInstance.firstName.set('');
    fixture.componentInstance.lastName.set('');

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error()).toBe('Completa nombre y apellido.');
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('submit en modo register llama a authService.register y navega a /cuenta', () => {
    authServiceSpy.register.mockReturnValue(of(authResponse));

    fixture.detectChanges();
    fixture.componentInstance.setMode('register');
    fixture.componentInstance.firstName.set('Ana');
    fixture.componentInstance.lastName.set('Test');
    fixture.componentInstance.email.set('ana@cinemahub.local');
    fixture.componentInstance.password.set('clave12345');

    fixture.componentInstance.submit();

    expect(authServiceSpy.register).toHaveBeenCalledWith({
      firstName: 'Ana',
      lastName: 'Test',
      email: 'ana@cinemahub.local',
      password: 'clave12345'
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/cuenta']);
  });

  it('submit en modo register setea error() cuando el email ya existe (409)', () => {
    authServiceSpy.register.mockReturnValue(
      throwError(() => ({ status: 409, message: 'Ya existe un usuario con ese email' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.setMode('register');
    fixture.componentInstance.firstName.set('Ana');
    fixture.componentInstance.lastName.set('Test');
    fixture.componentInstance.email.set('ana@cinemahub.local');
    fixture.componentInstance.password.set('clave12345');

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error()).toBe('Ya existe un usuario con ese email');
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AppError } from '../../core/interceptors/error.interceptor';
import { AuthService } from '../../core/services/auth.service';

type Mode = 'login' | 'register';

@Component({
  selector: 'app-login-page',
  standalone: true,
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  mode = signal<Mode>('login');
  loading = signal(false);
  error = signal<string | null>(null);

  email = signal('');
  password = signal('');
  firstName = signal('');
  lastName = signal('');

  setMode(mode: Mode): void {
    this.mode.set(mode);
    this.error.set(null);
  }

  onEmailChange(event: Event): void {
    this.email.set((event.target as HTMLInputElement).value);
  }

  onPasswordChange(event: Event): void {
    this.password.set((event.target as HTMLInputElement).value);
  }

  onFirstNameChange(event: Event): void {
    this.firstName.set((event.target as HTMLInputElement).value);
  }

  onLastNameChange(event: Event): void {
    this.lastName.set((event.target as HTMLInputElement).value);
  }

  submit(): void {
    const email = this.email().trim();
    const password = this.password();

    if (!email || !password) {
      this.error.set('Completa email y contraseña.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    if (this.mode() === 'login') {
      this.authService.login({ email, password }).subscribe({
        next: () => this.onSuccess(),
        error: (err: AppError) => this.onError(err)
      });
      return;
    }

    const firstName = this.firstName().trim();
    const lastName = this.lastName().trim();

    if (!firstName || !lastName) {
      this.error.set('Completa nombre y apellido.');
      this.loading.set(false);
      return;
    }

    this.authService.register({ firstName, lastName, email, password }).subscribe({
      next: () => this.onSuccess(),
      error: (err: AppError) => this.onError(err)
    });
  }

  private onSuccess(): void {
    this.loading.set(false);
    this.router.navigate(['/cuenta']);
  }

  private onError(err: AppError): void {
    if (err.fields) {
      console.log('Campos con error:', err.fields);
    // Aquí podrías mostrar los errores específicos por cada campo si lo deseas
    }
    this.error.set(err.message);
    this.loading.set(false);
  }
}
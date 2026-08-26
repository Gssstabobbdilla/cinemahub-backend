import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './core/services/auth.service';
import { CurrentUserService } from './core/services/current-user.service';
import { LoadingService } from './core/services/loading.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private loadingService = inject(LoadingService);
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser = inject(CurrentUserService);
  loading = this.loadingService.loading;

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/cartelera']);
  }
}
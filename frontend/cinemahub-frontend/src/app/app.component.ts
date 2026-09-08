import { Component, inject, signal } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';
import { filter } from 'rxjs';

import { AuthService } from './core/services/auth.service';
import { CurrentUserService } from './core/services/current-user.service';
import { LoadingService } from './core/services/loading.service';
import { NavBarComponent } from './shared/components/nav-bar/nav-bar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavBarComponent,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private loadingService = inject(LoadingService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  currentUser = inject(CurrentUserService);
  loading = this.loadingService.loading;

  // Controla si la navbar debe mostrarse
  // según la configuración de la ruta activa.
  showNav = signal(true);

  constructor() {
    this.router.events
      .pipe(
        filter(
          (event): event is NavigationEnd =>
            event instanceof NavigationEnd
        )
      )
      .subscribe(() => this.updateShowNav());

    this.updateShowNav();
  }

  private updateShowNav(): void {
    let route = this.activatedRoute.root;

    while (route.firstChild) {
      route = route.firstChild;
    }

    this.showNav.set(!route.snapshot.data['hideNav']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/cartelera']);
  }
}

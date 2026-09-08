import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  path: string;
}

// Barra superior estilo Cinemark: fondo negro, acento rojo, logo + links.
// No sabe nada de cuándo debe ocultarse — esa decisión la toma AppComponent
// leyendo `data: { hideNav: true }` de la ruta activa (ver login más abajo).
@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.scss'
})
export class NavBarComponent {
  mobileMenuOpen = signal(false);

  // TODO: cuando exista CurrentUserService con roles, filtrar "Administración"
  // para que solo aparezca si el usuario tiene ROLE_ADMIN.
  // NOTA: "Productos" todavía no tiene una ruta pública (solo existe bajo
  // /admin/productos) — avisame si querés que armemos una vista pública de
  // dulcería y la agrego acá.
  navItems: NavItem[] = [
    { label: 'Películas', path: '/cartelera' },
    { label: 'Checkout', path: '/checkout' },
    { label: 'Mi cuenta', path: '/cuenta' },
    { label: 'Administración', path: '/admin' }
  ];

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(open => !open);
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }
}
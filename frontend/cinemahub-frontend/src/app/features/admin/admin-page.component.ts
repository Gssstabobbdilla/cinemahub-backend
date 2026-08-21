import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminPageComponent {
  sections = [
    { path: 'productos', label: 'Productos', icon: '🍿' },
    { path: 'peliculas', label: 'Películas', icon: '🎬' },
    { path: 'cines', label: 'Cines y salas', icon: '🏢' },
    { path: 'funciones', label: 'Funciones', icon: '🕐' },
    { path: 'promociones', label: 'Promociones', icon: '🏷️' }
  ];
}
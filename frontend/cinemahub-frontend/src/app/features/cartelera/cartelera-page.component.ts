import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Movie } from '../../core/models/movie.model';
import { MovieService } from '../../core/services/movie.service';
import { MovieCardComponent } from '../../shared/components/movie-card/movie-card.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cartelera-page',
  standalone: true,
  imports: [CommonModule, MovieCardComponent], // <-- Asegúrate de tenerlo aquí
  templateUrl: './cartelera-page.component.html',
  styleUrl: './cartelera-page.component.scss'
})
export class CarteleraPageComponent implements OnInit {
  private movieService = inject(MovieService);

  movies = signal<Movie[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.movieService.findAll('ACTIVE').subscribe({
      next: movies => {
        this.movies.set(movies);
        this.loading.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }
}
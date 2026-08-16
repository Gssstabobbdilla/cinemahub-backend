import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Movie } from '../../core/models/movie.model';
import { Showtime } from '../../core/models/showtime.model';
import { MovieService } from '../../core/services/movie.service';
import { ShowtimeService } from '../../core/services/showtime.service';

@Component({
  selector: 'app-movie-detail-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './movie-detail-page.component.html',
  styleUrl: './movie-detail-page.component.scss'
})
export class MovieDetailPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private movieService = inject(MovieService);
  private showtimeService = inject(ShowtimeService);

  movie = signal<Movie | null>(null);
  showtimes = signal<Showtime[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const movieId = Number(this.route.snapshot.paramMap.get('movieId'));

    this.movieService.findById(movieId).subscribe({
      next: movie => this.movie.set(movie),
      error: (err: AppError) => this.error.set(err.message)
    });

    this.showtimeService.search({ movieId }).subscribe({
      next: showtimes => {
        this.showtimes.set(showtimes);
        this.loading.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }
}
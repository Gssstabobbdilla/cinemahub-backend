import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Classification, Genre, Movie, MovieStatus } from '../../core/models/movie.model';
import { ClassificationService } from '../../core/services/classification.service';
import { GenreService } from '../../core/services/genre.service';
import { MovieService } from '../../core/services/movie.service';

@Component({
  selector: 'app-peliculas-page',
  standalone: true,
  templateUrl: './peliculas-page.component.html',
  styleUrl: './peliculas-page.component.scss'
})
export class PeliculasPageComponent implements OnInit {
  private movieService = inject(MovieService);
  private classificationService = inject(ClassificationService);
  private genreService = inject(GenreService);

  movies = signal<Movie[]>([]);
  classifications = signal<Classification[]>([]);
  allGenres = signal<Genre[]>([]);
  movieGenres = signal<Record<number, Genre[]>>({});

  loading = signal(true);
  error = signal<string | null>(null);

  showForm = signal(false);
  editingMovie = signal<Movie | null>(null);
  saving = signal(false);

  formTitle = signal('');
  formSynopsis = signal('');
  formDuration = signal<number | null>(null);
  formReleaseDate = signal('');
  formPosterUrl = signal('');
  formTrailerUrl = signal('');
  formClassificationId = signal<number | null>(null);

  addingGenreTo = signal<number | null>(null);
  selectedGenreId = signal<number | null>(null);

  ngOnInit(): void {
    this.classificationService.findAll().subscribe({
      next: classifications => this.classifications.set(classifications)
    });
    this.genreService.findAll().subscribe({
      next: genres => this.allGenres.set(genres)
    });
    this.loadMovies();
  }

  private loadMovies(): void {
    this.loading.set(true);
    this.movieService.findAll().subscribe({
      next: movies => {
        this.movies.set(movies);
        this.loading.set(false);
        movies.forEach(m => this.loadGenresFor(m.id));
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  private loadGenresFor(movieId: number): void {
    this.movieService.findGenres(movieId).subscribe({
      next: genres => {
        this.movieGenres.update(current => ({ ...current, [movieId]: genres }));
      }
    });
  }

  genresFor(movieId: number): Genre[] {
    return this.movieGenres()[movieId] ?? [];
  }

  statusLabel(status: MovieStatus): string {
    const labels: Record<MovieStatus, string> = {
      ACTIVE: 'En cartelera',
      INACTIVE: 'Inactiva',
      COMING_SOON: 'Próximamente',
      ARCHIVED: 'Archivada'
    };
    return labels[status];
  }

  changeStatus(movie: Movie, event: Event): void {
    const status = (event.target as HTMLSelectElement).value as MovieStatus;
    this.movieService.changeStatus(movie.id, { status }).subscribe({
      next: updated => {
        this.movies.update(list => list.map(m => (m.id === updated.id ? updated : m)));
      },
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  openCreateForm(): void {
    this.editingMovie.set(null);
    this.formTitle.set('');
    this.formSynopsis.set('');
    this.formDuration.set(null);
    this.formReleaseDate.set('');
    this.formPosterUrl.set('');
    this.formTrailerUrl.set('');
    this.formClassificationId.set(this.classifications()[0]?.id ?? null);
    this.showForm.set(true);
  }

  openEditForm(movie: Movie): void {
    this.editingMovie.set(movie);
    this.formTitle.set(movie.title);
    this.formSynopsis.set(movie.synopsis ?? '');
    this.formDuration.set(movie.duration);
    this.formReleaseDate.set(movie.releaseDate ?? '');
    this.formPosterUrl.set(movie.posterUrl ?? '');
    this.formTrailerUrl.set(movie.trailerUrl ?? '');
    this.formClassificationId.set(movie.classification.id);
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.error.set(null);
  }

  onTitleChange(event: Event): void {
    this.formTitle.set((event.target as HTMLInputElement).value);
  }

  onSynopsisChange(event: Event): void {
    this.formSynopsis.set((event.target as HTMLTextAreaElement).value);
  }

  onDurationChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.formDuration.set(Number.isNaN(value) ? null : value);
  }

  onReleaseDateChange(event: Event): void {
    this.formReleaseDate.set((event.target as HTMLInputElement).value);
  }

  onPosterUrlChange(event: Event): void {
    this.formPosterUrl.set((event.target as HTMLInputElement).value);
  }

  onTrailerUrlChange(event: Event): void {
    this.formTrailerUrl.set((event.target as HTMLInputElement).value);
  }

  onClassificationChange(event: Event): void {
    this.formClassificationId.set(Number((event.target as HTMLSelectElement).value));
  }

  save(): void {
    const title = this.formTitle().trim();
    const duration = this.formDuration();
    const classificationId = this.formClassificationId();

    if (!title || !duration || !classificationId) {
      this.error.set('Completa título, duración y clasificación.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const editing = this.editingMovie();

    if (editing) {
      this.movieService
        .update(editing.id, {
          title,
          synopsis: this.formSynopsis() || undefined,
          duration,
          releaseDate: this.formReleaseDate() || undefined,
          posterUrl: this.formPosterUrl() || undefined,
          trailerUrl: this.formTrailerUrl() || undefined,
          classificationId
        })
        .subscribe({
          next: () => {
            this.saving.set(false);
            this.showForm.set(false);
            this.loadMovies();
          },
          error: (err: AppError) => {
            this.error.set(err.message);
            this.saving.set(false);
          }
        });
      return;
    }

    this.movieService.create({ title, duration, classificationId }).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.loadMovies();
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.saving.set(false);
      }
    });
  }

  startAddGenre(movieId: number): void {
    this.addingGenreTo.set(movieId);
    this.selectedGenreId.set(this.allGenres()[0]?.id ?? null);
  }

  cancelAddGenre(): void {
    this.addingGenreTo.set(null);
  }

  onSelectedGenreChange(event: Event): void {
    this.selectedGenreId.set(Number((event.target as HTMLSelectElement).value));
  }

  confirmAddGenre(movieId: number): void {
    const genreId = this.selectedGenreId();
    if (!genreId) {
      return;
    }
    this.movieService.addGenre(movieId, { genreId }).subscribe({
      next: () => {
        this.loadGenresFor(movieId);
        this.addingGenreTo.set(null);
      },
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  removeGenre(movieId: number, genreId: number): void {
    this.movieService.removeGenre(movieId, genreId).subscribe({
      next: () => this.loadGenresFor(movieId),
      error: (err: AppError) => this.error.set(err.message)
    });
  }
}
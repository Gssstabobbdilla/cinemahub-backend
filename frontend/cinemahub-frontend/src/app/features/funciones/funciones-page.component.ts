import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Cinema, Room } from '../../core/models/cinema.model';
import { Movie } from '../../core/models/movie.model';
import { Showtime } from '../../core/models/showtime.model';

import { CinemaService } from '../../core/services/cinema.service';
import { MovieService } from '../../core/services/movie.service';
import { RoomService } from '../../core/services/room.service';
import { ShowtimeService } from '../../core/services/showtime.service';

type SearchMode = 'movie' | 'room';

@Component({
  selector: 'app-funciones-page',
  standalone: true,
  templateUrl: './funciones-page.component.html',
  styleUrl: './funciones-page.component.scss'
})
export class FuncionesPageComponent implements OnInit {
  private movieService = inject(MovieService);
  private cinemaService = inject(CinemaService);
  private roomService = inject(RoomService);
  private showtimeService = inject(ShowtimeService);

  movies = signal<Movie[]>([]);
  cinemas = signal<Cinema[]>([]);
  rooms = signal<Room[]>([]);

  searchMode = signal<SearchMode>('movie');
  searchMovieId = signal<number | null>(null);
  searchCinemaId = signal<number | null>(null);
  searchRoomId = signal<number | null>(null);
  searchDate = signal<string>('');

  showtimes = signal<Showtime[]>([]);
  loading = signal(false);
  searched = signal(false);
  error = signal<string | null>(null);

  // --- form de nueva función ---
  showForm = signal(false);
  saving = signal(false);

  formMovieId = signal<number | null>(null);
  formCinemaId = signal<number | null>(null);
  formRoomId = signal<number | null>(null);
  formRooms = signal<Room[]>([]);
  formDate = signal('');
  formStartTime = signal('');
  formEndTime = signal('');
  formBasePrice = signal<number | null>(null);

  cancellingId = signal<number | null>(null);

  ngOnInit(): void {
    this.movieService.findAll().subscribe({
      next: movies => this.movies.set(movies)
    });
    this.cinemaService.findAll().subscribe({
      next: cinemas => this.cinemas.set(cinemas)
    });
  }

  setSearchMode(mode: SearchMode): void {
    this.searchMode.set(mode);
    this.showtimes.set([]);
    this.searched.set(false);
    this.error.set(null);
  }

  onSearchMovieChange(event: Event): void {
    this.searchMovieId.set(Number((event.target as HTMLSelectElement).value) || null);
  }

  onSearchCinemaChange(event: Event): void {
    const cinemaId = Number((event.target as HTMLSelectElement).value) || null;
    this.searchCinemaId.set(cinemaId);
    this.searchRoomId.set(null);
    this.rooms.set([]);

    if (cinemaId) {
      this.roomService.findByCinema(cinemaId).subscribe({
        next: rooms => this.rooms.set(rooms)
      });
    }
  }

  onSearchRoomChange(event: Event): void {
    this.searchRoomId.set(Number((event.target as HTMLSelectElement).value) || null);
  }

  onSearchDateChange(event: Event): void {
    this.searchDate.set((event.target as HTMLInputElement).value);
  }

  search(): void {
    this.error.set(null);

    if (this.searchMode() === 'movie') {
      const movieId = this.searchMovieId();
      if (!movieId) {
        this.error.set('Selecciona una película.');
        return;
      }
      this.loading.set(true);
      this.showtimeService.search({ movieId }).subscribe({
        next: showtimes => this.onSearchSuccess(showtimes),
        error: (err: AppError) => this.onSearchError(err)
      });
      return;
    }

    const roomId = this.searchRoomId();
    const date = this.searchDate();
    if (!roomId || !date) {
      this.error.set('Selecciona cine, sala y fecha.');
      return;
    }
    this.loading.set(true);
    this.showtimeService.search({ roomId, date }).subscribe({
      next: showtimes => this.onSearchSuccess(showtimes),
      error: (err: AppError) => this.onSearchError(err)
    });
  }

  private onSearchSuccess(showtimes: Showtime[]): void {
    this.showtimes.set(showtimes);
    this.loading.set(false);
    this.searched.set(true);
  }

  private onSearchError(err: AppError): void {
    this.error.set(err.message);
    this.loading.set(false);
    this.searched.set(true);
  }

  statusLabel(status: Showtime['status']): string {
    const labels: Record<Showtime['status'], string> = {
      SCHEDULED: 'Programada',
      IN_PROGRESS: 'En curso',
      FINISHED: 'Finalizada',
      CANCELLED: 'Cancelada'
    };
    return labels[status];
  }

  cancelShowtime(showtime: Showtime): void {
    this.cancellingId.set(showtime.id);
    this.error.set(null);

    this.showtimeService.cancel(showtime.id).subscribe({
      next: () => {
        this.showtimes.update(list =>
          list.map(s => (s.id === showtime.id ? { ...s, status: 'CANCELLED' } : s))
        );
        this.cancellingId.set(null);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.cancellingId.set(null);
      }
    });
  }

  // --- form de creación ---
  openCreateForm(): void {
    this.formMovieId.set(this.movies()[0]?.id ?? null);
    this.formCinemaId.set(null);
    this.formRoomId.set(null);
    this.formRooms.set([]);
    this.formDate.set('');
    this.formStartTime.set('');
    this.formEndTime.set('');
    this.formBasePrice.set(null);
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.error.set(null);
  }

  onFormMovieChange(event: Event): void {
    this.formMovieId.set(Number((event.target as HTMLSelectElement).value));
  }

  onFormCinemaChange(event: Event): void {
    const cinemaId = Number((event.target as HTMLSelectElement).value) || null;
    this.formCinemaId.set(cinemaId);
    this.formRoomId.set(null);
    this.formRooms.set([]);

    if (cinemaId) {
      this.roomService.findByCinema(cinemaId).subscribe({
        next: rooms => {
          this.formRooms.set(rooms);
          this.formRoomId.set(rooms[0]?.id ?? null);
        }
      });
    }
  }

  onFormRoomChange(event: Event): void {
    this.formRoomId.set(Number((event.target as HTMLSelectElement).value));
  }

  onFormDateChange(event: Event): void {
    this.formDate.set((event.target as HTMLInputElement).value);
  }

  onFormStartTimeChange(event: Event): void {
    this.formStartTime.set((event.target as HTMLInputElement).value);
  }

  onFormEndTimeChange(event: Event): void {
    this.formEndTime.set((event.target as HTMLInputElement).value);
  }

  onFormBasePriceChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.formBasePrice.set(Number.isNaN(value) ? null : value);
  }

  save(): void {
    const movieId = this.formMovieId();
    const roomId = this.formRoomId();
    const showDate = this.formDate();
    const startTime = this.formStartTime();
    const endTime = this.formEndTime();
    const basePrice = this.formBasePrice();

    if (!movieId || !roomId || !showDate || !startTime || !endTime || basePrice === null) {
      this.error.set('Completa todos los campos de la función.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    this.showtimeService
      .create({
        movieId,
        roomId,
        showDate,
        startTime: `${startTime}:00`,
        endTime: `${endTime}:00`,
        basePrice
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.showForm.set(false);
          // Si la búsqueda activa coincide con lo recién creado, la refrescamos.
          if (this.searched()) {
            this.search();
          }
        },
        error: (err: AppError) => {
          this.error.set(err.message);
          this.saving.set(false);
        }
      });
  }
}
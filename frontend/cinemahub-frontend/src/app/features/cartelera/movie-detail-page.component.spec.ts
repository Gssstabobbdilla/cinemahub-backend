import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter
} from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Movie } from '../../core/models/movie.model';
import { Showtime } from '../../core/models/showtime.model';
import { MovieService } from '../../core/services/movie.service';
import { ShowtimeService } from '../../core/services/showtime.service';
import { MovieDetailPageComponent } from './movie-detail-page.component';

describe('MovieDetailPageComponent', () => {
  let fixture: ComponentFixture<MovieDetailPageComponent>;

  let movieServiceSpy: {
    findById: ReturnType<typeof vi.fn>;
  };

  let showtimeServiceSpy: {
    search: ReturnType<typeof vi.fn>;
  };

  const movie: Movie = {
    id: 1,
    title: 'Dune: Parte 3',
    synopsis: 'Sinopsis de prueba',
    duration: 165,
    releaseDate: null,
    posterUrl: null,
    trailerUrl: null,
    classification: {
      id: 1,
      code: 'PG-13',
      description: null
    },
    status: 'ACTIVE'
  };

  const showtime: Showtime = {
    id: 10,
    movieId: 1,
    movieTitle: 'Dune: Parte 3',
    roomId: 2,
    roomName: 'Sala IMAX',
    cinemaName: 'Cineplanet Jockey Plaza',
    showDate: '2026-09-01',
    startTime: '19:30:00',
    endTime: '22:30:00',
    language: 'ES',
    format: '2D',
    basePrice: 35,
    status: 'SCHEDULED'
  };

  beforeEach(() => {
    movieServiceSpy = {
      findById: vi.fn()
    };

    showtimeServiceSpy = {
      search: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [MovieDetailPageComponent],

      providers: [
        // Primero configuramos el Router
        provideRouter([]),

        {
          provide: MovieService,
          useValue: movieServiceSpy
        },

        {
          provide: ShowtimeService,
          useValue: showtimeServiceSpy
        },

        // IMPORTANTE: el mock de ActivatedRoute va DESPUÉS
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                movieId: '1'
              })
            }
          }
        }
      ]
    });
  });

  it('pide la película y sus funciones usando el movieId de la ruta', () => {
    movieServiceSpy.findById.mockReturnValue(of(movie));
    showtimeServiceSpy.search.mockReturnValue(of([showtime]));

    fixture = TestBed.createComponent(MovieDetailPageComponent);
    fixture.detectChanges();

    expect(movieServiceSpy.findById).toHaveBeenCalledWith(1);

    expect(showtimeServiceSpy.search).toHaveBeenCalledWith({
      movieId: 1
    });

    expect(fixture.componentInstance.movie()).toEqual(movie);
    expect(fixture.componentInstance.showtimes()).toEqual([showtime]);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('setea error() cuando falla la carga de funciones', () => {
    movieServiceSpy.findById.mockReturnValue(of(movie));

    showtimeServiceSpy.search.mockReturnValue(
      throwError(() => ({
        status: 500,
        message: 'Error de conexión'
      }))
    );

    fixture = TestBed.createComponent(MovieDetailPageComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Error de conexión');
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
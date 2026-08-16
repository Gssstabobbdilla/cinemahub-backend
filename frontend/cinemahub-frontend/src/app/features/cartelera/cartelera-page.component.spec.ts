import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Movie } from '../../core/models/movie.model';
import { MovieService } from '../../core/services/movie.service';
import { CarteleraPageComponent } from './cartelera-page.component';

import {
  ActivatedRoute,
  provideRouter
} from '@angular/router';

describe('CarteleraPageComponent', () => {
  let fixture: ComponentFixture<CarteleraPageComponent>;
  let movieServiceSpy: {
    findAll: ReturnType<typeof vi.fn>;
  };

  const movie: Movie = {
    id: 1,
    title: 'Dune: Parte 3',
    synopsis: null,
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

  beforeEach(() => {
    movieServiceSpy = {
      findAll: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [CarteleraPageComponent],

      providers: [
        provideRouter([]),

        {
          provide: MovieService,
          useValue: movieServiceSpy
        },

        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: new Map()
            }
          }
        }
      ]
    });
  });

  it('empieza con loading en true', () => {
    movieServiceSpy.findAll.mockReturnValue(of([]));

    fixture = TestBed.createComponent(CarteleraPageComponent);

    expect(fixture.componentInstance.loading()).toBe(true);
  });

  it('pide las películas ACTIVE y las guarda al llegar', () => {
    movieServiceSpy.findAll.mockReturnValue(of([movie]));

    fixture = TestBed.createComponent(CarteleraPageComponent);
    fixture.detectChanges();

    expect(movieServiceSpy.findAll).toHaveBeenCalledWith('ACTIVE');
    expect(fixture.componentInstance.movies()).toEqual([movie]);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('setea error() cuando el service falla', () => {
    movieServiceSpy.findAll.mockReturnValue(
      throwError(() => ({
        status: 500,
        message: 'Error de conexión'
      }))
    );

    fixture = TestBed.createComponent(CarteleraPageComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Error de conexión');
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
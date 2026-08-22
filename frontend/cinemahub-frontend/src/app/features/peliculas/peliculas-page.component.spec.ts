import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Classification, Genre, Movie } from '../../core/models/movie.model';
import { ClassificationService } from '../../core/services/classification.service';
import { GenreService } from '../../core/services/genre.service';
import { MovieService } from '../../core/services/movie.service';

import { PeliculasPageComponent } from './peliculas-page.component';

describe('PeliculasPageComponent', () => {
  let fixture: ComponentFixture<PeliculasPageComponent>;

  let movieServiceSpy: {
    findAll: ReturnType<typeof vi.fn>;
    findGenres: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    changeStatus: ReturnType<typeof vi.fn>;
    addGenre: ReturnType<typeof vi.fn>;
    removeGenre: ReturnType<typeof vi.fn>;
  };

  let classificationServiceSpy: { findAll: ReturnType<typeof vi.fn> };
  let genreServiceSpy: { findAll: ReturnType<typeof vi.fn> };

  const classification: Classification = { id: 1, code: 'PG-13', description: null };

  const movie: Movie = {
    id: 10,
    title: 'Dune: Parte 3',
    synopsis: null,
    duration: 165,
    releaseDate: null,
    posterUrl: null,
    trailerUrl: null,
    classification,
    status: 'ACTIVE'
  };

  const genres: Genre[] = [{ id: 2, name: 'Ciencia ficción' }];

  beforeEach(() => {
    movieServiceSpy = {
      findAll: vi.fn(),
      findGenres: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      changeStatus: vi.fn(),
      addGenre: vi.fn(),
      removeGenre: vi.fn()
    };
    classificationServiceSpy = { findAll: vi.fn() };
    genreServiceSpy = { findAll: vi.fn() };

    classificationServiceSpy.findAll.mockReturnValue(of([classification]));
    genreServiceSpy.findAll.mockReturnValue(of(genres));
    movieServiceSpy.findAll.mockReturnValue(of([movie]));
    movieServiceSpy.findGenres.mockReturnValue(of(genres));

    TestBed.configureTestingModule({
      imports: [PeliculasPageComponent],
      providers: [
        { provide: MovieService, useValue: movieServiceSpy },
        { provide: ClassificationService, useValue: classificationServiceSpy },
        { provide: GenreService, useValue: genreServiceSpy }
      ]
    });

    fixture = TestBed.createComponent(PeliculasPageComponent);
  });

  it('carga clasificaciones, géneros y películas (con sus géneros) al iniciar', () => {
    fixture.detectChanges();

    expect(movieServiceSpy.findAll).toHaveBeenCalled();
    expect(movieServiceSpy.findGenres).toHaveBeenCalledWith(10);
    expect(fixture.componentInstance.movies()).toEqual([movie]);
    expect(fixture.componentInstance.genresFor(10)).toEqual(genres);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('setea error() cuando falla la carga de películas', () => {
    movieServiceSpy.findAll.mockReturnValue(
      throwError(() => ({ status: 500, message: 'Error de conexión' }))
    );

    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Error de conexión');
  });

  it('changeStatus actualiza el status de la película en la lista', () => {
    const updated: Movie = { ...movie, status: 'COMING_SOON' };
    movieServiceSpy.changeStatus.mockReturnValue(of(updated));

    fixture.detectChanges();

    const fakeEvent = { target: { value: 'COMING_SOON' } } as unknown as Event;
    fixture.componentInstance.changeStatus(movie, fakeEvent);

    expect(movieServiceSpy.changeStatus).toHaveBeenCalledWith(10, { status: 'COMING_SOON' });
    expect(fixture.componentInstance.movies()[0].status).toBe('COMING_SOON');
  });

  it('save valida título, duración y clasificación', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formTitle.set('');

    fixture.componentInstance.save();

    expect(fixture.componentInstance.error()).toBe('Completa título, duración y clasificación.');
    expect(movieServiceSpy.create).not.toHaveBeenCalled();
  });

  it('save crea una película nueva cuando no hay editingMovie', () => {
    movieServiceSpy.create.mockReturnValue(of(movie));

    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formTitle.set('Dune: Parte 3');
    fixture.componentInstance.formDuration.set(165);
    fixture.componentInstance.formClassificationId.set(1);

    fixture.componentInstance.save();

    expect(movieServiceSpy.create).toHaveBeenCalledWith({
      title: 'Dune: Parte 3',
      duration: 165,
      classificationId: 1
    });
    expect(fixture.componentInstance.showForm()).toBe(false);
  });

  it('save actualiza la película cuando hay editingMovie', () => {
    const updated: Movie = { ...movie, title: 'Dune: Parte 3 Extendida' };
    movieServiceSpy.update.mockReturnValue(of(updated));

    fixture.detectChanges();
    fixture.componentInstance.openEditForm(movie);
    fixture.componentInstance.formTitle.set('Dune: Parte 3 Extendida');
    fixture.componentInstance.formPosterUrl.set('https://x.com/p.png');

    fixture.componentInstance.save();

    expect(movieServiceSpy.update).toHaveBeenCalledWith(10, {
      title: 'Dune: Parte 3 Extendida',
      synopsis: undefined,
      duration: 165,
      releaseDate: undefined,
      posterUrl: 'https://x.com/p.png',
      trailerUrl: undefined,
      classificationId: 1
    });
    expect(movieServiceSpy.create).not.toHaveBeenCalled();
  });

  it('confirmAddGenre agrega el género seleccionado y recarga la lista de géneros', () => {
    movieServiceSpy.addGenre.mockReturnValue(of(undefined));

    fixture.detectChanges();
    fixture.componentInstance.startAddGenre(10);
    fixture.componentInstance.selectedGenreId.set(2);

    fixture.componentInstance.confirmAddGenre(10);

    expect(movieServiceSpy.addGenre).toHaveBeenCalledWith(10, { genreId: 2 });
    expect(fixture.componentInstance.addingGenreTo()).toBeNull();
  });

  it('removeGenre elimina el género y recarga la lista', () => {
    movieServiceSpy.removeGenre.mockReturnValue(of(undefined));

    fixture.detectChanges();
    fixture.componentInstance.removeGenre(10, 2);

    expect(movieServiceSpy.removeGenre).toHaveBeenCalledWith(10, 2);
    expect(movieServiceSpy.findGenres).toHaveBeenCalledWith(10);
  });
});
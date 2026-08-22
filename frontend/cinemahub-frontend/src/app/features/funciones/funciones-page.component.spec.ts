import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Cinema, Room } from '../../core/models/cinema.model';
import { Movie } from '../../core/models/movie.model';
import { Showtime } from '../../core/models/showtime.model';

import { CinemaService } from '../../core/services/cinema.service';
import { MovieService } from '../../core/services/movie.service';
import { RoomService } from '../../core/services/room.service';
import { ShowtimeService } from '../../core/services/showtime.service';

import { FuncionesPageComponent } from './funciones-page.component';

describe('FuncionesPageComponent', () => {
  let fixture: ComponentFixture<FuncionesPageComponent>;

  let movieServiceSpy: { findAll: ReturnType<typeof vi.fn> };
  let cinemaServiceSpy: { findAll: ReturnType<typeof vi.fn> };
  let roomServiceSpy: { findByCinema: ReturnType<typeof vi.fn> };
  let showtimeServiceSpy: {
    search: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    cancel: ReturnType<typeof vi.fn>;
  };

  const movie: Movie = {
    id: 3,
    title: 'Interestelar 2',
    synopsis: null,
    duration: 180,
    releaseDate: null,
    posterUrl: null,
    trailerUrl: null,
    classification: { id: 1, code: 'PG-13', description: null },
    status: 'ACTIVE'
  };

  const cinema: Cinema = {
    id: 1,
    name: 'Cineplanet Jockey Plaza',
    department: null,
    province: null,
    district: null,
    address: null,
    phone: null,
    latitude: null,
    longitude: null
  };

  const room: Room = { id: 2, cinemaId: 1, name: 'Sala IMAX', type: 'IMAX', capacity: 200 };

  const showtime: Showtime = {
    id: 10,
    movieId: 3,
    movieTitle: 'Interestelar 2',
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
    movieServiceSpy = { findAll: vi.fn() };
    cinemaServiceSpy = { findAll: vi.fn() };
    roomServiceSpy = { findByCinema: vi.fn() };
    showtimeServiceSpy = { search: vi.fn(), create: vi.fn(), cancel: vi.fn() };

    movieServiceSpy.findAll.mockReturnValue(of([movie]));
    cinemaServiceSpy.findAll.mockReturnValue(of([cinema]));
    roomServiceSpy.findByCinema.mockReturnValue(of([room]));

    TestBed.configureTestingModule({
      imports: [FuncionesPageComponent],
      providers: [
        { provide: MovieService, useValue: movieServiceSpy },
        { provide: CinemaService, useValue: cinemaServiceSpy },
        { provide: RoomService, useValue: roomServiceSpy },
        { provide: ShowtimeService, useValue: showtimeServiceSpy }
      ]
    });

    fixture = TestBed.createComponent(FuncionesPageComponent);
  });

  it('carga películas y cines al iniciar', () => {
    fixture.detectChanges();

    expect(movieServiceSpy.findAll).toHaveBeenCalled();
    expect(cinemaServiceSpy.findAll).toHaveBeenCalled();
    expect(fixture.componentInstance.movies()).toEqual([movie]);
    expect(fixture.componentInstance.cinemas()).toEqual([cinema]);
  });

  it('search por película valida que haya una seleccionada', () => {
    fixture.detectChanges();
    fixture.componentInstance.setSearchMode('movie');
    fixture.componentInstance.searchMovieId.set(null);

    fixture.componentInstance.search();

    expect(fixture.componentInstance.error()).toBe('Selecciona una película.');
    expect(showtimeServiceSpy.search).not.toHaveBeenCalled();
  });

  it('search por película llama al service con movieId', () => {
    showtimeServiceSpy.search.mockReturnValue(of([showtime]));

    fixture.detectChanges();
    fixture.componentInstance.setSearchMode('movie');
    fixture.componentInstance.searchMovieId.set(3);

    fixture.componentInstance.search();

    expect(showtimeServiceSpy.search).toHaveBeenCalledWith({ movieId: 3 });
    expect(fixture.componentInstance.showtimes()).toEqual([showtime]);
    expect(fixture.componentInstance.searched()).toBe(true);
  });

  it('search por cine/sala/fecha valida que estén completos', () => {
    fixture.detectChanges();
    fixture.componentInstance.setSearchMode('room');
    fixture.componentInstance.searchRoomId.set(null);
    fixture.componentInstance.searchDate.set('');

    fixture.componentInstance.search();

    expect(fixture.componentInstance.error()).toBe('Selecciona cine, sala y fecha.');
    expect(showtimeServiceSpy.search).not.toHaveBeenCalled();
  });

  it('search por cine/sala/fecha llama al service con roomId y date', () => {
    showtimeServiceSpy.search.mockReturnValue(of([showtime]));

    fixture.detectChanges();
    fixture.componentInstance.setSearchMode('room');
    fixture.componentInstance.searchRoomId.set(2);
    fixture.componentInstance.searchDate.set('2026-09-01');

    fixture.componentInstance.search();

    expect(showtimeServiceSpy.search).toHaveBeenCalledWith({ roomId: 2, date: '2026-09-01' });
  });

  it('onSearchCinemaChange carga las salas de ese cine', () => {
    fixture.detectChanges();

    const fakeEvent = { target: { value: '1' } } as unknown as Event;
    fixture.componentInstance.onSearchCinemaChange(fakeEvent);

    expect(roomServiceSpy.findByCinema).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.rooms()).toEqual([room]);
  });

  it('cancelShowtime marca la función como CANCELLED en la lista sin recargar todo', () => {
    showtimeServiceSpy.search.mockReturnValue(of([showtime]));
    showtimeServiceSpy.cancel.mockReturnValue(of({ ...showtime, status: 'CANCELLED' }));

    fixture.detectChanges();
    fixture.componentInstance.setSearchMode('movie');
    fixture.componentInstance.searchMovieId.set(3);
    fixture.componentInstance.search();

    fixture.componentInstance.cancelShowtime(showtime);

    expect(showtimeServiceSpy.cancel).toHaveBeenCalledWith(10);
    expect(fixture.componentInstance.showtimes()[0].status).toBe('CANCELLED');
  });

  it('cancelShowtime setea error() si falla', () => {
    showtimeServiceSpy.search.mockReturnValue(of([showtime]));
    showtimeServiceSpy.cancel.mockReturnValue(
      throwError(() => ({ status: 500, message: 'Error de conexión' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.setSearchMode('movie');
    fixture.componentInstance.searchMovieId.set(3);
    fixture.componentInstance.search();

    fixture.componentInstance.cancelShowtime(showtime);

    expect(fixture.componentInstance.error()).toBe('Error de conexión');
  });

  it('openCreateForm precarga la primera película disponible', () => {
    fixture.detectChanges();

    fixture.componentInstance.openCreateForm();

    expect(fixture.componentInstance.showForm()).toBe(true);
    expect(fixture.componentInstance.formMovieId()).toBe(3);
  });

  it('onFormCinemaChange carga las salas del cine y preselecciona la primera', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();

    const fakeEvent = { target: { value: '1' } } as unknown as Event;
    fixture.componentInstance.onFormCinemaChange(fakeEvent);

    expect(roomServiceSpy.findByCinema).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.formRooms()).toEqual([room]);
    expect(fixture.componentInstance.formRoomId()).toBe(2);
  });

  it('save valida que todos los campos estén completos', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formRoomId.set(null);

    fixture.componentInstance.save();

    expect(fixture.componentInstance.error()).toBe('Completa todos los campos de la función.');
    expect(showtimeServiceSpy.create).not.toHaveBeenCalled();
  });

  it('save crea la función con los campos completos y formatea las horas con segundos', () => {
    showtimeServiceSpy.create.mockReturnValue(of(showtime));

    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formMovieId.set(3);
    fixture.componentInstance.formRoomId.set(2);
    fixture.componentInstance.formDate.set('2026-09-01');
    fixture.componentInstance.formStartTime.set('19:30');
    fixture.componentInstance.formEndTime.set('22:30');
    fixture.componentInstance.formBasePrice.set(35);

    fixture.componentInstance.save();

    expect(showtimeServiceSpy.create).toHaveBeenCalledWith({
      movieId: 3,
      roomId: 2,
      showDate: '2026-09-01',
      startTime: '19:30:00',
      endTime: '22:30:00',
      basePrice: 35
    });
    expect(fixture.componentInstance.showForm()).toBe(false);
  });

  it('save setea error() cuando el backend responde 409 por función duplicada', () => {
    showtimeServiceSpy.create.mockReturnValue(
      throwError(() => ({ status: 409, message: 'Ya existe una función' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formMovieId.set(3);
    fixture.componentInstance.formRoomId.set(2);
    fixture.componentInstance.formDate.set('2026-09-01');
    fixture.componentInstance.formStartTime.set('19:30');
    fixture.componentInstance.formEndTime.set('22:30');
    fixture.componentInstance.formBasePrice.set(35);

    fixture.componentInstance.save();

    expect(fixture.componentInstance.error()).toBe('Ya existe una función');
    expect(fixture.componentInstance.showForm()).toBe(true);
  });
});
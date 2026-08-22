import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Cinema, Room, Seat } from '../../core/models/cinema.model';
import { CinemaService } from '../../core/services/cinema.service';
import { RoomService } from '../../core/services/room.service';
import { SeatService } from '../../core/services/seat.service';

import { CinesPageComponent } from './cines-page.component';

describe('CinesPageComponent', () => {
  let fixture: ComponentFixture<CinesPageComponent>;

  let cinemaServiceSpy: {
    findAll: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };

  let roomServiceSpy: {
    findByCinema: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };

  let seatServiceSpy: {
    findByRoom: ReturnType<typeof vi.fn>;
    generateSeats: ReturnType<typeof vi.fn>;
  };

  const cinema: Cinema = {
    id: 1,
    name: 'Cineplanet Alcázar',
    department: 'Lima',
    province: 'Lima',
    district: 'Miraflores',
    address: null,
    phone: null,
    latitude: null,
    longitude: null
  };

  const room: Room = { id: 5, cinemaId: 1, name: 'Sala 1', type: 'STANDARD', capacity: 80 };

  const seats: Seat[] = [
    { id: 1, roomId: 5, rowLabel: 'A', seatNumber: 1, seatType: 'STANDARD' },
    { id: 2, roomId: 5, rowLabel: 'A', seatNumber: 2, seatType: 'STANDARD' }
  ];

  beforeEach(() => {
    cinemaServiceSpy = {
      findAll: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn()
    };

    roomServiceSpy = {
      findByCinema: vi.fn(),
      create: vi.fn(),
      delete: vi.fn()
    };

    seatServiceSpy = {
      findByRoom: vi.fn(),
      generateSeats: vi.fn()
    };

    cinemaServiceSpy.findAll.mockReturnValue(of([cinema]));
    roomServiceSpy.findByCinema.mockReturnValue(of([room]));
    seatServiceSpy.findByRoom.mockReturnValue(of(seats));

    TestBed.configureTestingModule({
      imports: [CinesPageComponent],
      providers: [
        { provide: CinemaService, useValue: cinemaServiceSpy },
        { provide: RoomService, useValue: roomServiceSpy },
        { provide: SeatService, useValue: seatServiceSpy }
      ]
    });

    fixture = TestBed.createComponent(CinesPageComponent);
  });

  it('carga la lista de cines al iniciar', () => {
    fixture.detectChanges();

    expect(cinemaServiceSpy.findAll).toHaveBeenCalled();
    expect(fixture.componentInstance.cinemas()).toEqual([cinema]);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('setea error() cuando falla la carga de cines', () => {
    cinemaServiceSpy.findAll.mockReturnValue(
      throwError(() => ({ status: 500, message: 'Error de conexión' }))
    );

    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Error de conexión');
  });

  it('selectCinema carga las salas y sus conteos de asientos', () => {
    fixture.detectChanges();

    fixture.componentInstance.selectCinema(cinema);

    expect(roomServiceSpy.findByCinema).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.selectedCinema()).toEqual(cinema);
    expect(fixture.componentInstance.rooms()).toEqual([room]);
    expect(seatServiceSpy.findByRoom).toHaveBeenCalledWith(5);
    expect(fixture.componentInstance.seatCountFor(5)).toBe(2);
  });

  it('saveCinema valida que el nombre no esté vacío', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCreateCinemaForm();
    fixture.componentInstance.formName.set('');

    fixture.componentInstance.saveCinema();

    expect(fixture.componentInstance.error()).toBe('El nombre del cine es obligatorio.');
    expect(cinemaServiceSpy.create).not.toHaveBeenCalled();
  });

  it('saveCinema crea un cine nuevo cuando no hay editingCinema', () => {
    cinemaServiceSpy.create.mockReturnValue(of(cinema));

    fixture.detectChanges();
    fixture.componentInstance.openCreateCinemaForm();
    fixture.componentInstance.formName.set('Cineplanet Alcázar');

    fixture.componentInstance.saveCinema();

    expect(cinemaServiceSpy.create).toHaveBeenCalledWith({ name: 'Cineplanet Alcázar' });
    expect(fixture.componentInstance.showCinemaForm()).toBe(false);
  });

  it('saveCinema actualiza el cine cuando hay editingCinema', () => {
    const updated: Cinema = { ...cinema, name: 'Cineplanet Renovado' };
    cinemaServiceSpy.update.mockReturnValue(of(updated));

    fixture.detectChanges();
    fixture.componentInstance.openEditCinemaForm(cinema);
    fixture.componentInstance.formName.set('Cineplanet Renovado');

    fixture.componentInstance.saveCinema();

    expect(cinemaServiceSpy.update).toHaveBeenCalledWith(1, {
      name: 'Cineplanet Renovado',
      department: 'Lima',
      province: 'Lima',
      district: 'Miraflores',
      address: undefined
    });
    expect(cinemaServiceSpy.create).not.toHaveBeenCalled();
  });

  it('deleteCinema elimina el cine y limpia la selección si estaba seleccionado', () => {
    cinemaServiceSpy.delete.mockReturnValue(of(undefined));

    fixture.detectChanges();
    fixture.componentInstance.selectCinema(cinema);
    fixture.componentInstance.deleteCinema(cinema);

    expect(cinemaServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.selectedCinema()).toBeNull();
    expect(fixture.componentInstance.rooms()).toEqual([]);
  });

  it('saveRoom valida nombre y capacidad, y crea la sala en el cine seleccionado', () => {
    roomServiceSpy.create.mockReturnValue(of(room));

    fixture.detectChanges();
    fixture.componentInstance.selectCinema(cinema);
    fixture.componentInstance.openCreateRoomForm();
    fixture.componentInstance.roomName.set('Sala 1');
    fixture.componentInstance.roomCapacity.set(80);

    fixture.componentInstance.saveRoom();

    expect(roomServiceSpy.create).toHaveBeenCalledWith(1, { name: 'Sala 1', capacity: 80 });
    expect(fixture.componentInstance.showRoomForm()).toBe(false);
  });

  it('saveRoom no llama al service si falta el cine seleccionado', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCreateRoomForm();
    fixture.componentInstance.roomName.set('Sala 1');
    fixture.componentInstance.roomCapacity.set(80);

    fixture.componentInstance.saveRoom();

    expect(roomServiceSpy.create).not.toHaveBeenCalled();
    expect(fixture.componentInstance.error()).toBe('Completa nombre y capacidad de la sala.');
  });

  it('deleteRoom elimina la sala y recarga la lista de salas', () => {
    roomServiceSpy.delete.mockReturnValue(of(undefined));

    fixture.detectChanges();
    fixture.componentInstance.selectCinema(cinema);
    fixture.componentInstance.deleteRoom(room);

    expect(roomServiceSpy.delete).toHaveBeenCalledWith(5);
    expect(roomServiceSpy.findByCinema).toHaveBeenCalledWith(1);
  });

  it('confirmGenerateSeats valida filas y asientos por fila', () => {
    fixture.detectChanges();
    fixture.componentInstance.openSeatGenerator(room);
    fixture.componentInstance.seatRowCount.set(null);

    fixture.componentInstance.confirmGenerateSeats(5);

    expect(seatServiceSpy.generateSeats).not.toHaveBeenCalled();
    expect(fixture.componentInstance.error()).toBe('Completa filas y asientos por fila.');
  });

  it('confirmGenerateSeats genera los asientos y actualiza el conteo', () => {
    const generated: Seat[] = [
      { id: 1, roomId: 5, rowLabel: 'A', seatNumber: 1, seatType: 'STANDARD' },
      { id: 2, roomId: 5, rowLabel: 'A', seatNumber: 2, seatType: 'STANDARD' },
      { id: 3, roomId: 5, rowLabel: 'B', seatNumber: 1, seatType: 'STANDARD' }
    ];
    seatServiceSpy.generateSeats.mockReturnValue(of(generated));

    fixture.detectChanges();
    fixture.componentInstance.openSeatGenerator(room);
    fixture.componentInstance.seatRowCount.set(2);
    fixture.componentInstance.seatsPerRow.set(3);

    fixture.componentInstance.confirmGenerateSeats(5);

    expect(seatServiceSpy.generateSeats).toHaveBeenCalledWith(5, { rowCount: 2, seatsPerRow: 3 });
    expect(fixture.componentInstance.seatCountFor(5)).toBe(3);
    expect(fixture.componentInstance.generatingSeatsFor()).toBeNull();
  });
});
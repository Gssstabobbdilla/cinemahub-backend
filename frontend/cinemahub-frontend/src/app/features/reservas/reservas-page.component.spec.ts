import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Reservation, ShowtimeSeat } from '../../core/models/reservation.model';
import { Showtime } from '../../core/models/showtime.model';
import { ReservationService } from '../../core/services/reservation.service';
import { ShowtimeService } from '../../core/services/showtime.service';
import { ReservasPageComponent } from './reservas-page.component';

describe('ReservasPageComponent', () => {
  let fixture: ComponentFixture<ReservasPageComponent>;

  let showtimeServiceSpy: {
    findById: ReturnType<typeof vi.fn>;
  };

  let reservationServiceSpy: {
    findShowtimeSeats: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
  };

  let routerSpy: {
    navigate: ReturnType<typeof vi.fn>;
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
    basePrice: 25,
    status: 'SCHEDULED'
  };

  const showtimeSeats: ShowtimeSeat[] = [
    {
      seatId: 1,
      rowLabel: 'A',
      seatNumber: 1,
      seatType: 'STANDARD',
      taken: true
    },
    {
      seatId: 2,
      rowLabel: 'A',
      seatNumber: 2,
      seatType: 'STANDARD',
      taken: false
    }
  ];

  beforeEach(() => {
    showtimeServiceSpy = {
      findById: vi.fn()
    };

    reservationServiceSpy = {
      findShowtimeSeats: vi.fn(),
      create: vi.fn()
    };

    routerSpy = {
      navigate: vi.fn()
    };

    showtimeServiceSpy.findById.mockReturnValue(of(showtime));
    reservationServiceSpy.findShowtimeSeats.mockReturnValue(of(showtimeSeats));

    TestBed.configureTestingModule({
      imports: [ReservasPageComponent],
      providers: [
        {
          provide: ShowtimeService,
          useValue: showtimeServiceSpy
        },
        {
          provide: ReservationService,
          useValue: reservationServiceSpy
        },
        {
          provide: Router,
          useValue: routerSpy
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                showtimeId: '10'
              })
            }
          }
        }
      ]
    });

    fixture = TestBed.createComponent(ReservasPageComponent);
  });

  it('mapea ShowtimeSeat a Seat y separa los IDs ya tomados', () => {
    fixture.detectChanges();

    expect(
      fixture.componentInstance.seats().map(s => s.id)
    ).toEqual([1, 2]);

    expect(
      fixture.componentInstance.takenSeatIds()
    ).toEqual([1]);

    expect(
      fixture.componentInstance.loading()
    ).toBe(false);
  });

  it('onSelectionChange actualiza selectedSeatIds y el total calculado', () => {
    fixture.detectChanges();

    fixture.componentInstance.onSelectionChange([2]);

    expect(
      fixture.componentInstance.selectedSeatIds()
    ).toEqual([2]);

    expect(
      fixture.componentInstance.total()
    ).toBe(25);
  });

  it('confirmReservation no hace nada sin userId ni asientos seleccionados', () => {
    fixture.detectChanges();

    fixture.componentInstance.confirmReservation();

    expect(
      reservationServiceSpy.create
    ).not.toHaveBeenCalled();
  });

  it('confirmReservation crea la reserva y navega a checkout', () => {
    const reservation: Reservation = {
      id: 50,
      userId: 1,
      status: 'PENDING',
      expiresAt: 'x',
      createdAt: 'x'
    };

    reservationServiceSpy.create.mockReturnValue(of(reservation));

    fixture.detectChanges();

    fixture.componentInstance.userId.set(1);
    fixture.componentInstance.onSelectionChange([2]);

    fixture.componentInstance.confirmReservation();

    expect(
      reservationServiceSpy.create
    ).toHaveBeenCalledWith({
      userId: 1,
      showtimeId: 10,
      seatIds: [2]
    });

    expect(
      routerSpy.navigate
    ).toHaveBeenCalledWith(['/checkout', 50]);
  });

  it('confirmReservation setea error() cuando el asiento ya fue tomado (409)', () => {
    reservationServiceSpy.create.mockReturnValue(
      throwError(() => ({
        status: 409,
        message: 'Ya existe una reserva con ese asiento'
      }))
    );

    fixture.detectChanges();

    fixture.componentInstance.userId.set(1);
    fixture.componentInstance.onSelectionChange([2]);

    fixture.componentInstance.confirmReservation();

    expect(
      fixture.componentInstance.error()
    ).toBe('Ya existe una reserva con ese asiento');

    expect(
      fixture.componentInstance.submitting()
    ).toBe(false);
  });
});

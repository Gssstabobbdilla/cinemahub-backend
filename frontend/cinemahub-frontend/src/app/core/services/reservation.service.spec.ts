import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CreateReservationRequest, Reservation, ReservationSeat } from '../models/reservation.model';
import { ReservationService } from './reservation.service';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;
  const reservation: Reservation = {
    id: 50,
    userId: 1,
    status: 'PENDING',
    expiresAt: '2026-08-14T10:15:00-05:00',
    createdAt: '2026-08-14T10:05:00-05:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ReservationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('create hace POST /reservations con el body correcto', () => {
    const request: CreateReservationRequest = { userId: 1, showtimeId: 5, seatIds: [10, 11] };
    service.create(request).subscribe(res => expect(res).toEqual(reservation));
    const req = httpMock.expectOne(`${apiUrl}/reservations`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(reservation);
  });

  it('findById hace GET /reservations/:id', () => {
    service.findById(50).subscribe(res => expect(res).toEqual(reservation));
    const req = httpMock.expectOne(`${apiUrl}/reservations/50`);
    expect(req.request.method).toBe('GET');
    req.flush(reservation);
  });

  it('findSeats hace GET /reservations/:id/seats', () => {
    const seats: ReservationSeat[] = [{ seatId: 10, rowLabel: 'A', seatNumber: 1, price: 25 }];
    service.findSeats(50).subscribe(res => expect(res).toEqual(seats));
    const req = httpMock.expectOne(`${apiUrl}/reservations/50/seats`);
    expect(req.request.method).toBe('GET');
    req.flush(seats);
  });

  it('findByUser hace GET /users/:userId/reservations', () => {
    service.findByUser(1).subscribe(res => expect(res).toEqual([reservation]));
    const req = httpMock.expectOne(`${apiUrl}/users/1/reservations`);
    expect(req.request.method).toBe('GET');
    req.flush([reservation]);
  });

  it('confirm hace POST /reservations/:id/confirm', () => {
    service.confirm(50).subscribe(res => expect(res).toEqual(reservation));
    const req = httpMock.expectOne(`${apiUrl}/reservations/50/confirm`);
    expect(req.request.method).toBe('POST');
    req.flush(reservation);
  });

  it('cancel hace POST /reservations/:id/cancel', () => {
    service.cancel(50).subscribe(res => expect(res).toEqual(reservation));
    const req = httpMock.expectOne(`${apiUrl}/reservations/50/cancel`);
    expect(req.request.method).toBe('POST');
    req.flush(reservation);
  });
});
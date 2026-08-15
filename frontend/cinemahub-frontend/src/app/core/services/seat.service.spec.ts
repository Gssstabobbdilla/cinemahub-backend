import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Seat } from '../models/cinema.model';
import { SeatService } from './seat.service';

describe('SeatService', () => {
  let service: SeatService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;
  const seat: Seat = { id: 100, roomId: 2, rowLabel: 'C', seatNumber: 7, seatType: 'STANDARD' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(SeatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findByRoom hace GET /rooms/:roomId/seats', () => {
    service.findByRoom(2).subscribe(res => expect(res).toEqual([seat]));
    const req = httpMock.expectOne(`${apiUrl}/rooms/2/seats`);
    expect(req.request.method).toBe('GET');
    req.flush([seat]);
  });

  it('create hace POST /rooms/:roomId/seats con el body correcto', () => {
    service.create(2, { rowLabel: 'C', seatNumber: 7 }).subscribe(res => expect(res).toEqual(seat));
    const req = httpMock.expectOne(`${apiUrl}/rooms/2/seats`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ rowLabel: 'C', seatNumber: 7 });
    req.flush(seat);
  });

  it('generateSeats hace POST /rooms/:roomId/seats/generate con el body correcto', () => {
    service.generateSeats(2, { rowCount: 5, seatsPerRow: 10 }).subscribe(res => expect(res).toEqual([seat]));
    const req = httpMock.expectOne(`${apiUrl}/rooms/2/seats/generate`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ rowCount: 5, seatsPerRow: 10 });
    req.flush([seat]);
  });

  it('findById hace GET /seats/:id', () => {
    service.findById(100).subscribe(res => expect(res).toEqual(seat));
    const req = httpMock.expectOne(`${apiUrl}/seats/100`);
    expect(req.request.method).toBe('GET');
    req.flush(seat);
  });

  it('delete hace DELETE /seats/:id', () => {
    service.delete(100).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/seats/100`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
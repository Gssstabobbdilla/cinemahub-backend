import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CreateShowtimeRequest, Showtime } from '../models/showtime.model';
import { ShowtimeService } from './showtime.service';

describe('ShowtimeService', () => {
  let service: ShowtimeService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/showtimes`;
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
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ShowtimeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('search con movieId agrega solo ese query param', () => {
    service.search({ movieId: 3 }).subscribe(res => expect(res).toEqual([showtime]));
    const req = httpMock.expectOne(
      r => r.url === baseUrl && r.params.get('movieId') === '3' && !r.params.has('roomId') && !r.params.has('date')
    );
    expect(req.request.method).toBe('GET');
    req.flush([showtime]);
  });

  it('search con roomId+date agrega ambos query params', () => {
    service.search({ roomId: 2, date: '2026-09-01' }).subscribe(res => expect(res).toEqual([showtime]));
    const req = httpMock.expectOne(
      r => r.url === baseUrl && r.params.get('roomId') === '2' && r.params.get('date') === '2026-09-01'
    );
    expect(req.request.method).toBe('GET');
    req.flush([showtime]);
  });

  it('findById hace GET /showtimes/:id', () => {
    service.findById(10).subscribe(res => expect(res).toEqual(showtime));
    const req = httpMock.expectOne(`${baseUrl}/10`);
    expect(req.request.method).toBe('GET');
    req.flush(showtime);
  });

  it('create hace POST /showtimes con el body correcto', () => {
    const request: CreateShowtimeRequest = {
      movieId: 3,
      roomId: 2,
      showDate: '2026-09-01',
      startTime: '19:30:00',
      endTime: '22:30:00',
      basePrice: 35
    };
    service.create(request).subscribe(res => expect(res).toEqual(showtime));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(showtime);
  });

  it('cancel hace DELETE /showtimes/:id', () => {
    service.cancel(10).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
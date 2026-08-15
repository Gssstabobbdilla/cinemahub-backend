import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Room } from '../models/cinema.model';
import { RoomService } from './room.service';

describe('RoomService', () => {
  let service: RoomService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;
  const room: Room = { id: 5, cinemaId: 1, name: 'Sala 3', type: 'STANDARD', capacity: 80 };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RoomService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findByCinema hace GET /cinemas/:cinemaId/rooms', () => {
    service.findByCinema(1).subscribe(res => expect(res).toEqual([room]));
    const req = httpMock.expectOne(`${apiUrl}/cinemas/1/rooms`);
    expect(req.request.method).toBe('GET');
    req.flush([room]);
  });

  it('create hace POST /cinemas/:cinemaId/rooms con el body correcto (no repite cinemaId en el body)', () => {
    service.create(1, { name: 'Sala 3', capacity: 80 }).subscribe(res => expect(res).toEqual(room));
    const req = httpMock.expectOne(`${apiUrl}/cinemas/1/rooms`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Sala 3', capacity: 80 });
    req.flush(room);
  });

  it('findById hace GET /rooms/:id (no /cinemas/.../rooms/:id)', () => {
    service.findById(5).subscribe(res => expect(res).toEqual(room));
    const req = httpMock.expectOne(`${apiUrl}/rooms/5`);
    expect(req.request.method).toBe('GET');
    req.flush(room);
  });

  it('delete hace DELETE /rooms/:id', () => {
    service.delete(5).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/rooms/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
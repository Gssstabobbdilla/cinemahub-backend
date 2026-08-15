import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Cinema } from '../models/cinema.model';
import { CinemaService } from './cinema.service';

describe('CinemaService', () => {
  let service: CinemaService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/cinemas`;
  const cinema: Cinema = {
    id: 1,
    name: 'Cineplanet Alcázar',
    department: null,
    province: null,
    district: null,
    address: null,
    phone: null,
    latitude: null,
    longitude: null
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CinemaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /cinemas', () => {
    service.findAll().subscribe(res => expect(res).toEqual([cinema]));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([cinema]);
  });

  it('findById hace GET /cinemas/:id', () => {
    service.findById(1).subscribe(res => expect(res).toEqual(cinema));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(cinema);
  });

  it('create hace POST /cinemas con el body correcto', () => {
    service.create({ name: 'Cineplanet Alcázar' }).subscribe(res => expect(res).toEqual(cinema));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Cineplanet Alcázar' });
    req.flush(cinema);
  });

  it('updateLocation hace PUT /cinemas/:id/location con el body correcto', () => {
    const request = { department: 'Lima', province: 'Lima', district: 'Miraflores', address: 'Av. X 123' };
    service.updateLocation(1, request).subscribe(res => expect(res).toEqual(cinema));
    const req = httpMock.expectOne(`${baseUrl}/1/location`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(cinema);
  });

  it('delete hace DELETE /cinemas/:id', () => {
    service.delete(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
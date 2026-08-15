import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Genre, GenreRequest } from '../models/movie.model';
import { GenreService } from './genre.service';

describe('GenreService', () => {
  let service: GenreService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/genres`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(GenreService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /genres', () => {
    const mock: Genre[] = [{ id: 1, name: 'Drama' }];
    service.findAll().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('findById hace GET /genres/:id', () => {
    const mock: Genre = { id: 1, name: 'Drama' };
    service.findById(1).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('create hace POST /genres con el body correcto', () => {
    const request: GenreRequest = { name: 'Ciencia ficción' };
    const mock: Genre = { id: 1, ...request };
    service.create(request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('update hace PUT /genres/:id con el body correcto', () => {
    const request: GenreRequest = { name: 'Terror' };
    const mock: Genre = { id: 1, ...request };
    service.update(1, request).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('delete hace DELETE /genres/:id', () => {
    service.delete(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
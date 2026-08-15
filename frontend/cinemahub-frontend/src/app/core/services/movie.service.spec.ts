import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Classification, CreateMovieRequest, Genre, Movie } from '../models/movie.model';
import { MovieService } from './movie.service';

describe('MovieService', () => {
  let service: MovieService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/movies`;

  const classification: Classification = { id: 1, code: 'PG-13', description: null };
  const movie: Movie = {
    id: 1,
    title: 'Dune: Parte 3',
    synopsis: null,
    duration: 165,
    releaseDate: null,
    posterUrl: null,
    trailerUrl: null,
    classification,
    status: 'ACTIVE'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MovieService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll sin status hace GET /movies sin query params', () => {
    service.findAll().subscribe(res => expect(res).toEqual([movie]));
    const req = httpMock.expectOne(r => r.url === baseUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('status')).toBe(false);
    req.flush([movie]);
  });

  it('findAll con status agrega el query param correcto', () => {
    service.findAll('COMING_SOON').subscribe(res => expect(res).toEqual([movie]));
    const req = httpMock.expectOne(r => r.url === baseUrl && r.params.get('status') === 'COMING_SOON');
    expect(req.request.method).toBe('GET');
    req.flush([movie]);
  });

  it('findById hace GET /movies/:id', () => {
    service.findById(1).subscribe(res => expect(res).toEqual(movie));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(movie);
  });

  it('create hace POST /movies con el body correcto', () => {
    const request: CreateMovieRequest = { title: 'Dune: Parte 3', duration: 165, classificationId: 1 };
    service.create(request).subscribe(res => expect(res).toEqual(movie));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(movie);
  });

  it('changeStatus hace PATCH /movies/:id/status con el body correcto', () => {
    service.changeStatus(1, { status: 'ARCHIVED' }).subscribe(res => expect(res).toEqual(movie));
    const req = httpMock.expectOne(`${baseUrl}/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'ARCHIVED' });
    req.flush(movie);
  });

  it('findGenres hace GET /movies/:id/genres', () => {
    const genres: Genre[] = [{ id: 2, name: 'Ciencia ficción' }];
    service.findGenres(1).subscribe(res => expect(res).toEqual(genres));
    const req = httpMock.expectOne(`${baseUrl}/1/genres`);
    expect(req.request.method).toBe('GET');
    req.flush(genres);
  });

  it('addGenre hace POST /movies/:id/genres con el body correcto', () => {
    service.addGenre(1, { genreId: 2 }).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/genres`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ genreId: 2 });
    req.flush(null);
  });

  it('removeGenre hace DELETE /movies/:id/genres/:genreId', () => {
    service.removeGenre(1, 2).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/genres/2`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
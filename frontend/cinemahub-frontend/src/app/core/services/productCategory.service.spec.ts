import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ProductCategory } from '../models/product.model';
import { ProductCategoryService } from './productCategory.service';

describe('ProductCategoryService', () => {
  let service: ProductCategoryService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/product-categories`;
  const category: ProductCategory = { id: 1, name: 'Snacks' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProductCategoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findAll hace GET /product-categories', () => {
    service.findAll().subscribe(res => expect(res).toEqual([category]));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([category]);
  });

  it('findById hace GET /product-categories/:id', () => {
    service.findById(1).subscribe(res => expect(res).toEqual(category));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(category);
  });

  it('create hace POST /product-categories con el body correcto', () => {
    service.create({ name: 'Snacks' }).subscribe(res => expect(res).toEqual(category));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Snacks' });
    req.flush(category);
  });

  it('update hace PUT /product-categories/:id con el body correcto', () => {
    service.update(1, { name: 'Bebidas' }).subscribe(res => expect(res).toEqual(category));
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ name: 'Bebidas' });
    req.flush(category);
  });

  it('delete hace DELETE /product-categories/:id', () => {
    service.delete(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
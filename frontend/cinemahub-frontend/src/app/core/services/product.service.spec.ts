import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CreateProductRequest, InventoryMovement, Product } from '../models/product.model';
import { ProductService } from './product.service';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/products`;
  const product: Product = {
    id: 5,
    categoryId: 1,
    categoryName: 'Snacks',
    name: 'Nachos',
    description: null,
    imageUrl: null,
    price: 12,
    stock: 0,
    status: 'ACTIVE'
  };


  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('search con categoryId agrega ese query param', () => {
    service.search({ categoryId: 1 }).subscribe(res => expect(res).toEqual([product]));
    const req = httpMock.expectOne(r => r.url === baseUrl && r.params.get('categoryId') === '1');
    expect(req.request.method).toBe('GET');
    req.flush([product]);
  });

  it('search con status agrega ese query param', () => {
    service.search({ status: 'ACTIVE' }).subscribe(res => expect(res).toEqual([product]));
    const req = httpMock.expectOne(r => r.url === baseUrl && r.params.get('status') === 'ACTIVE');
    expect(req.request.method).toBe('GET');
    req.flush([product]);
  });

  it('findById hace GET /products/:id', () => {
    service.findById(5).subscribe(res => expect(res).toEqual(product));
    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('GET');
    req.flush(product);
  });

  it('create hace POST /products con el body correcto', () => {
    const request: CreateProductRequest = { categoryId: 1, name: 'Nachos', price: 12 };
    service.create(request).subscribe(res => expect(res).toEqual(product));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(product);
  });

  it('adjustStock hace POST /products/:id/stock con el body correcto', () => {
    const movement: InventoryMovement = { id: 1, productId: 5, movementType: 'IN', quantity: 100, createdAt: 'x' };
    service.adjustStock(5, { movementType: 'IN', quantity: 100 }).subscribe(res => expect(res).toEqual(movement));
    const req = httpMock.expectOne(`${baseUrl}/5/stock`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ movementType: 'IN', quantity: 100 });
    req.flush(movement);
  });

  it('findMovements hace GET /products/:id/movements', () => {
    const movements: InventoryMovement[] = [{ id: 1, productId: 5, movementType: 'IN', quantity: 100, createdAt: 'x' }];
    service.findMovements(5).subscribe(res => expect(res).toEqual(movements));
    const req = httpMock.expectOne(`${baseUrl}/5/movements`);
    expect(req.request.method).toBe('GET');
    req.flush(movements);
  });
});
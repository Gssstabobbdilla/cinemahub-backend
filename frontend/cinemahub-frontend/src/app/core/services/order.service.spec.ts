import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { AddProductRequest, Order, OrderProduct, Payment, RegisterPaymentRequest } from '../models/order.model';
import { OrderService } from './order.service';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;
  const order: Order = { id: 1, reservationId: 7, total: 50, status: 'PENDING', purchasedAt: null };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('createFromReservation hace POST /reservations/:reservationId/order', () => {
    service.createFromReservation(7).subscribe(res => expect(res).toEqual(order));
    const req = httpMock.expectOne(`${apiUrl}/reservations/7/order`);
    expect(req.request.method).toBe('POST');
    req.flush(order);
  });

  it('findById hace GET /orders/:id', () => {
    service.findById(1).subscribe(res => expect(res).toEqual(order));
    const req = httpMock.expectOne(`${apiUrl}/orders/1`);
    expect(req.request.method).toBe('GET');
    req.flush(order);
  });

  it('findProducts hace GET /orders/:id/products', () => {
    const products: OrderProduct[] = [
      { productId: 3, productName: 'Canchita', quantity: 2, unitPrice: 12.5, lineTotal: 25 }
    ];
    service.findProducts(1).subscribe(res => expect(res).toEqual(products));
    const req = httpMock.expectOne(`${apiUrl}/orders/1/products`);
    expect(req.request.method).toBe('GET');
    req.flush(products);
  });

  it('addProduct hace POST /orders/:id/products con el body correcto', () => {
    const request: AddProductRequest = { productId: 3, quantity: 2 };
    const product: OrderProduct = { productId: 3, productName: 'Canchita', quantity: 2, unitPrice: 12.5, lineTotal: 25 };
    service.addProduct(1, request).subscribe(res => expect(res).toEqual(product));
    const req = httpMock.expectOne(`${apiUrl}/orders/1/products`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(product);
  });

  it('registerPayment hace POST /orders/:id/payments con el body correcto', () => {
    const request: RegisterPaymentRequest = { paymentMethod: 'CARD', transactionCode: 'TX-1', approved: true };
    const payment: Payment = {
      id: 1,
      orderId: 1,
      paymentMethod: 'CARD',
      transactionCode: 'TX-1',
      amount: 50,
      status: 'APPROVED',
      paidAt: '2026-08-14T10:20:00-05:00'
    };
    service.registerPayment(1, request).subscribe(res => expect(res).toEqual(payment));
    const req = httpMock.expectOne(`${apiUrl}/orders/1/payments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(payment);
  });

  it('cancel hace POST /orders/:id/cancel', () => {
    service.cancel(1).subscribe(res => expect(res).toEqual(order));
    const req = httpMock.expectOne(`${apiUrl}/orders/1/cancel`);
    expect(req.request.method).toBe('POST');
    req.flush(order);
  });

  it('findByReservation hace GET /reservations/:reservationId/order', () => {
    service.findByReservation(7).subscribe(res => expect(res).toEqual(order));
    const req = httpMock.expectOne(`${apiUrl}/reservations/7/order`);
    expect(req.request.method).toBe('GET');
    req.flush(order);
  });
});
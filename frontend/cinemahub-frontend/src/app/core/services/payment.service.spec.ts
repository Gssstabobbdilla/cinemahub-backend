import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Payment } from '../models/order.model';
import { PaymentService } from './payment.service';

describe('PaymentService', () => {
  let service: PaymentService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;
  const payment: Payment = {
    id: 3,
    orderId: 1,
    paymentMethod: 'CARD',
    transactionCode: 'TX-1',
    amount: 20,
    status: 'APPROVED',
    paidAt: '2026-08-14T10:20:00-05:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PaymentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('findById hace GET /payments/:id', () => {
    service.findById(3).subscribe(res => expect(res).toEqual(payment));
    const req = httpMock.expectOne(`${apiUrl}/payments/3`);
    expect(req.request.method).toBe('GET');
    req.flush(payment);
  });

  it('findByOrder hace GET /orders/:orderId/payments', () => {
    service.findByOrder(1).subscribe(res => expect(res).toEqual([payment]));
    const req = httpMock.expectOne(`${apiUrl}/orders/1/payments`);
    expect(req.request.method).toBe('GET');
    req.flush([payment]);
  });

  it('refund hace POST /payments/:id/refund', () => {
    const refunded: Payment = { ...payment, status: 'REFUNDED' };
    service.refund(3).subscribe(res => expect(res).toEqual(refunded));
    const req = httpMock.expectOne(`${apiUrl}/payments/3/refund`);
    expect(req.request.method).toBe('POST');
    req.flush(refunded);
  });
});
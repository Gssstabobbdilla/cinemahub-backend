import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Order, Payment } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';

import { PagosPageComponent } from './pagos-page.component';

describe('PagosPageComponent', () => {
  let fixture: ComponentFixture<PagosPageComponent>;

  let orderServiceSpy: { findById: ReturnType<typeof vi.fn> };
  let paymentServiceSpy: {
    findByOrder: ReturnType<typeof vi.fn>;
    refund: ReturnType<typeof vi.fn>;
  };

  const order: Order = { id: 1, reservationId: 7, total: 50, status: 'PAID', purchasedAt: 'x' };

  const payment: Payment = {
    id: 3,
    orderId: 1,
    paymentMethod: 'CARD',
    transactionCode: 'SIM-123',
    amount: 50,
    status: 'APPROVED',
    paidAt: 'x'
  };

  beforeEach(() => {
    orderServiceSpy = { findById: vi.fn() };
    paymentServiceSpy = { findByOrder: vi.fn(), refund: vi.fn() };

    TestBed.configureTestingModule({
      imports: [PagosPageComponent],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy },
        { provide: PaymentService, useValue: paymentServiceSpy }
      ]
    });

    fixture = TestBed.createComponent(PagosPageComponent);
  });

  it('search valida que haya un ID de orden ingresado', () => {
    fixture.detectChanges();

    fixture.componentInstance.search();

    expect(fixture.componentInstance.error()).toBe('Ingresa un ID de orden válido.');
    expect(orderServiceSpy.findById).not.toHaveBeenCalled();
  });

  it('search encuentra la orden y carga sus pagos', () => {
    orderServiceSpy.findById.mockReturnValue(of(order));
    paymentServiceSpy.findByOrder.mockReturnValue(of([payment]));

    fixture.detectChanges();
    fixture.componentInstance.searchOrderId.set(1);

    fixture.componentInstance.search();

    expect(orderServiceSpy.findById).toHaveBeenCalledWith(1);
    expect(paymentServiceSpy.findByOrder).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.order()).toEqual(order);
    expect(fixture.componentInstance.payments()).toEqual([payment]);
    expect(fixture.componentInstance.searched()).toBe(true);
  });

  it('search setea error() cuando la orden no existe (404)', () => {
    orderServiceSpy.findById.mockReturnValue(
      throwError(() => ({ status: 404, message: 'Order no encontrado: id=99' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.searchOrderId.set(99);

    fixture.componentInstance.search();

    expect(fixture.componentInstance.error()).toBe('Order no encontrado: id=99');
    expect(fixture.componentInstance.searched()).toBe(true);
    expect(fixture.componentInstance.order()).toBeNull();
  });

  it('refund reembolsa el pago, lo actualiza en la lista y refresca la orden', () => {
    orderServiceSpy.findById.mockReturnValue(of(order));
    paymentServiceSpy.findByOrder.mockReturnValue(of([payment]));

    fixture.detectChanges();
    fixture.componentInstance.searchOrderId.set(1);
    fixture.componentInstance.search();

    const refundedPayment: Payment = { ...payment, status: 'REFUNDED' };
    const refundedOrder: Order = { ...order, status: 'REFUNDED' };

    paymentServiceSpy.refund.mockReturnValue(of(refundedPayment));
    orderServiceSpy.findById.mockReturnValue(of(refundedOrder));

    fixture.componentInstance.refund(payment);

    expect(paymentServiceSpy.refund).toHaveBeenCalledWith(3);
    expect(fixture.componentInstance.payments()).toEqual([refundedPayment]);
    expect(fixture.componentInstance.order()).toEqual(refundedOrder);
    expect(fixture.componentInstance.refundingId()).toBeNull();
  });

  it('refund setea error() cuando el pago no estaba APPROVED (400)', () => {
    orderServiceSpy.findById.mockReturnValue(of(order));
    paymentServiceSpy.findByOrder.mockReturnValue(of([payment]));

    fixture.detectChanges();
    fixture.componentInstance.searchOrderId.set(1);
    fixture.componentInstance.search();

    paymentServiceSpy.refund.mockReturnValue(
      throwError(() => ({ status: 400, message: 'Solo un pago APPROVED puede reembolsarse' }))
    );

    fixture.componentInstance.refund(payment);

    expect(fixture.componentInstance.error()).toBe('Solo un pago APPROVED puede reembolsarse');
    expect(fixture.componentInstance.refundingId()).toBeNull();
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Order, OrderProduct, Payment } from '../../core/models/order.model';
import { Product } from '../../core/models/product.model';
import { ReservationSeat } from '../../core/models/reservation.model';

import { OrderService } from '../../core/services/order.service';
import { ProductService } from '../../core/services/product.service';
import { ReservationService } from '../../core/services/reservation.service';

import { CheckoutPageComponent } from './checkout-page.component';

describe('CheckoutPageComponent', () => {
  let fixture: ComponentFixture<CheckoutPageComponent>;

  let orderServiceSpy: {
    createFromReservation: ReturnType<typeof vi.fn>;
    findByReservation: ReturnType<typeof vi.fn>;
    findById: ReturnType<typeof vi.fn>;
    findProducts: ReturnType<typeof vi.fn>;
    addProduct: ReturnType<typeof vi.fn>;
    registerPayment: ReturnType<typeof vi.fn>;
    cancel: ReturnType<typeof vi.fn>;
  };

  let productServiceSpy: { search: ReturnType<typeof vi.fn> };
  let reservationServiceSpy: { findSeats: ReturnType<typeof vi.fn> };
  let routerSpy: { navigate: ReturnType<typeof vi.fn> };

  const order: Order = { id: 1, reservationId: 7, total: 50, status: 'PENDING', purchasedAt: null };

  const seats: ReservationSeat[] = [
    { seatId: 1, rowLabel: 'A', seatNumber: 1, price: 25 },
    { seatId: 2, rowLabel: 'A', seatNumber: 2, price: 25 }
  ];

  const products: Product[] = [
    {
      id: 3,
      categoryId: 1,
      categoryName: 'Snacks',
      name: 'Canchita',
      description: null,
      imageUrl: null,
      price: 12.5,
      stock: 10,
      status: 'ACTIVE'
    }
  ];

  beforeEach(() => {
    orderServiceSpy = {
      createFromReservation: vi.fn(),
      findByReservation: vi.fn(),
      findById: vi.fn(),
      findProducts: vi.fn(),
      addProduct: vi.fn(),
      registerPayment: vi.fn(),
      cancel: vi.fn()
    };

    productServiceSpy = { search: vi.fn() };
    reservationServiceSpy = { findSeats: vi.fn() };
    routerSpy = { navigate: vi.fn() };

    reservationServiceSpy.findSeats.mockReturnValue(of(seats));
    productServiceSpy.search.mockReturnValue(of(products));
    orderServiceSpy.findProducts.mockReturnValue(of([]));

    TestBed.configureTestingModule({
      imports: [CheckoutPageComponent],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy },
        { provide: ProductService, useValue: productServiceSpy },
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ reservationId: '7' }) } }
        }
      ]
    });

    fixture = TestBed.createComponent(CheckoutPageComponent);
  });

  it('crea la orden desde la reserva y carga asientos, productos y orderProducts', () => {
    orderServiceSpy.createFromReservation.mockReturnValue(of(order));

    fixture.detectChanges();

    expect(orderServiceSpy.createFromReservation).toHaveBeenCalledWith(7);
    expect(fixture.componentInstance.order()).toEqual(order);
    expect(fixture.componentInstance.seats()).toEqual(seats);
    expect(fixture.componentInstance.availableProducts()).toEqual(products);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('si la orden ya existía (409), la recupera con findByReservation', () => {
    orderServiceSpy.createFromReservation.mockReturnValue(
      throwError(() => ({ status: 409, message: 'Ya existe una orden' }))
    );
    orderServiceSpy.findByReservation.mockReturnValue(of(order));

    fixture.detectChanges();

    expect(orderServiceSpy.findByReservation).toHaveBeenCalledWith(7);
    expect(fixture.componentInstance.order()).toEqual(order);
    expect(fixture.componentInstance.error()).toBeNull();
  });

  it('setea error() cuando falla la creación por un motivo que no es 409', () => {
    orderServiceSpy.createFromReservation.mockReturnValue(
      throwError(() => ({ status: 404, message: 'Reservation no encontrado' }))
    );

    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Reservation no encontrado');
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('addProduct agrega el producto y refresca orderProducts y el total de la orden', () => {
    orderServiceSpy.createFromReservation.mockReturnValue(of(order));
    fixture.detectChanges();

    const orderProduct: OrderProduct = {
      productId: 3,
      productName: 'Canchita',
      quantity: 1,
      unitPrice: 12.5,
      lineTotal: 12.5
    };
    const updatedOrder: Order = { ...order, total: 62.5 };

    orderServiceSpy.addProduct.mockReturnValue(of(orderProduct));
    orderServiceSpy.findProducts.mockReturnValue(of([orderProduct]));
    orderServiceSpy.findById.mockReturnValue(of(updatedOrder));

    fixture.componentInstance.addProduct(3);

    expect(orderServiceSpy.addProduct).toHaveBeenCalledWith(1, { productId: 3, quantity: 1 });
    expect(fixture.componentInstance.orderProducts()).toEqual([orderProduct]);
    expect(fixture.componentInstance.order()).toEqual(updatedOrder);
  });

  it('pay registra el pago simulado con approved true y marca paid()', () => {
    orderServiceSpy.createFromReservation.mockReturnValue(of(order));
    fixture.detectChanges();

    const payment: Payment = {
      id: 9,
      orderId: 1,
      paymentMethod: 'CARD',
      transactionCode: 'SIM-123',
      amount: 50,
      status: 'APPROVED',
      paidAt: 'x'
    };
    const paidOrder: Order = { ...order, status: 'PAID', purchasedAt: 'x' };

    orderServiceSpy.registerPayment.mockReturnValue(of(payment));
    orderServiceSpy.findById.mockReturnValue(of(paidOrder));

    fixture.componentInstance.pay();

    expect(orderServiceSpy.registerPayment).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ paymentMethod: 'CARD', approved: true })
    );
    expect(fixture.componentInstance.paid()).toBe(true);
    expect(fixture.componentInstance.order()).toEqual(paidOrder);
  });

  it('cancelOrder cancela la orden y actualiza su estado', () => {
    orderServiceSpy.createFromReservation.mockReturnValue(of(order));
    fixture.detectChanges();

    const cancelledOrder: Order = { ...order, status: 'CANCELLED' };
    orderServiceSpy.cancel.mockReturnValue(of(cancelledOrder));

    fixture.componentInstance.cancelOrder();

    expect(orderServiceSpy.cancel).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.order()).toEqual(cancelledOrder);
  });
});
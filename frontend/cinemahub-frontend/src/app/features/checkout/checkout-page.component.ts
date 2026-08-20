import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Order, OrderProduct } from '../../core/models/order.model';
import { Product } from '../../core/models/product.model';
import { ReservationSeat } from '../../core/models/reservation.model';

import { OrderService } from '../../core/services/order.service';
import { ProductService } from '../../core/services/product.service';
import { ReservationService } from '../../core/services/reservation.service';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  templateUrl: './checkout-page.component.html',
  styleUrl: './checkout-page.component.scss'
})
export class CheckoutPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private productService = inject(ProductService);
  private reservationService = inject(ReservationService);

  private reservationId!: number;

  order = signal<Order | null>(null);
  seats = signal<ReservationSeat[]>([]);
  orderProducts = signal<OrderProduct[]>([]);
  availableProducts = signal<Product[]>([]);
  quantities = signal<Record<number, number>>({});

  paymentMethod = signal('CARD');
  paid = signal(false);

  loading = signal(true);
  error = signal<string | null>(null);
  addingProduct = signal(false);
  paying = signal(false);
  cancelling = signal(false);

  ngOnInit(): void {
    this.reservationId = Number(this.route.snapshot.paramMap.get('reservationId'));

    this.reservationService.findSeats(this.reservationId).subscribe({
      next: seats => this.seats.set(seats),
      error: (err: AppError) => this.error.set(err.message)
    });

    this.productService.search({ status: 'ACTIVE' }).subscribe({
      next: products => this.availableProducts.set(products)
    });

    this.orderService.createFromReservation(this.reservationId).subscribe({
      next: order => this.onOrderReady(order),
      error: (err: AppError) => {
        // La orden ya existía (recarga a mitad del checkout, doble click, etc.):
        // recuperamos la existente en vez de tratarlo como un error real.
        if (err.status === 409) {
          this.orderService.findByReservation(this.reservationId).subscribe({
            next: order => this.onOrderReady(order),
            error: (recoverErr: AppError) => {
              this.error.set(recoverErr.message);
              this.loading.set(false);
            }
          });
          return;
        }
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  private onOrderReady(order: Order): void {
    this.order.set(order);
    this.loadOrderProducts(order.id);
    this.loading.set(false);
  }

  private loadOrderProducts(orderId: number): void {
    this.orderService.findProducts(orderId).subscribe({
      next: products => this.orderProducts.set(products),
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  private refreshOrder(): void {
    const order = this.order();
    if (!order) {
      return;
    }
    this.orderService.findById(order.id).subscribe({
      next: updated => this.order.set(updated),
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  onQuantityChange(productId: number, event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.quantities.update(current => ({
      ...current,
      [productId]: Number.isNaN(value) || value < 1 ? 1 : value
    }));
  }

  quantityFor(productId: number): number {
    return this.quantities()[productId] ?? 1;
  }

  addProduct(productId: number): void {
    const order = this.order();
    if (!order) {
      return;
    }

    this.addingProduct.set(true);
    this.error.set(null);

    this.orderService.addProduct(order.id, { productId, quantity: this.quantityFor(productId) }).subscribe({
      next: () => {
        this.loadOrderProducts(order.id);
        this.refreshOrder();
        this.addingProduct.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.addingProduct.set(false);
      }
    });
  }

  onPaymentMethodChange(event: Event): void {
    this.paymentMethod.set((event.target as HTMLSelectElement).value);
  }

  pay(): void {
    const order = this.order();
    if (!order) {
      return;
    }

    this.paying.set(true);
    this.error.set(null);

    // approved: true es un pago simulado (ver TODO en RegisterPaymentRequest del backend);
    // cuando se integre una pasarela real, este valor lo determina esa respuesta.
    this.orderService
      .registerPayment(order.id, {
        paymentMethod: this.paymentMethod(),
        transactionCode: `SIM-${Date.now()}`,
        approved: true
      })
      .subscribe({
        next: () => {
          this.refreshOrder();
          this.paid.set(true);
          this.paying.set(false);
        },
        error: (err: AppError) => {
          this.error.set(err.message);
          this.paying.set(false);
        }
      });
  }

  cancelOrder(): void {
    const order = this.order();
    if (!order) {
      return;
    }

    this.cancelling.set(true);
    this.error.set(null);

    this.orderService.cancel(order.id).subscribe({
      next: updated => {
        this.order.set(updated);
        this.cancelling.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.cancelling.set(false);
      }
    });
  }

  goToAccount(): void {
    this.router.navigate(['/cuenta']);
  }
}
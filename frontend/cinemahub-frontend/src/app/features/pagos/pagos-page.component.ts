import { Component, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Order, Payment } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';

@Component({
  selector: 'app-pagos-page',
  standalone: true,
  templateUrl: './pagos-page.component.html',
  styleUrl: './pagos-page.component.scss'
})
export class PagosPageComponent {
  private orderService = inject(OrderService);
  private paymentService = inject(PaymentService);

  searchOrderId = signal<number | null>(null);
  order = signal<Order | null>(null);
  payments = signal<Payment[]>([]);

  searching = signal(false);
  searched = signal(false);
  error = signal<string | null>(null);

  refundingId = signal<number | null>(null);

  onSearchOrderIdChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.searchOrderId.set(Number.isNaN(value) ? null : value);
  }

  search(): void {
    const orderId = this.searchOrderId();
    if (!orderId) {
      this.error.set('Ingresa un ID de orden válido.');
      return;
    }

    this.searching.set(true);
    this.error.set(null);
    this.order.set(null);
    this.payments.set([]);

    this.orderService.findById(orderId).subscribe({
      next: order => {
        this.order.set(order);
        this.loadPayments(orderId);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.searching.set(false);
        this.searched.set(true);
      }
    });
  }

  private loadPayments(orderId: number): void {
    this.paymentService.findByOrder(orderId).subscribe({
      next: payments => {
        this.payments.set(payments);
        this.searching.set(false);
        this.searched.set(true);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.searching.set(false);
        this.searched.set(true);
      }
    });
  }

  statusLabel(status: Payment['status']): string {
    const labels: Record<Payment['status'], string> = {
      PENDING: 'Pendiente',
      APPROVED: 'Aprobado',
      REJECTED: 'Rechazado',
      REFUNDED: 'Reembolsado'
    };
    return labels[status];
  }

  refund(payment: Payment): void {
    this.refundingId.set(payment.id);
    this.error.set(null);

    this.paymentService.refund(payment.id).subscribe({
      next: refunded => {
        this.payments.update(list => list.map(p => (p.id === refunded.id ? refunded : p)));
        this.refundingId.set(null);
        // El reembolso también marca la orden como REFUNDED en el backend
        // (ver PaymentService.refund) — refrescamos para reflejar ese estado.
        const orderId = this.order()?.id;
        if (orderId) {
          this.orderService.findById(orderId).subscribe({ next: order => this.order.set(order) });
        }
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.refundingId.set(null);
      }
    });
  }
}
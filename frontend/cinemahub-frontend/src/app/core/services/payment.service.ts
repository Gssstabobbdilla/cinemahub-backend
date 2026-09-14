import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Payment } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  findById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/payments/${id}`);
  }

  findByOrder(orderId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/orders/${orderId}/payments`);
  }

  // Historial de pagos del usuario autenticado, para "Mi cuenta".
  findByUser(userId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/users/${userId}/payments`);
  }

  refund(id: number): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/payments/${id}/refund`, {});
  }
}
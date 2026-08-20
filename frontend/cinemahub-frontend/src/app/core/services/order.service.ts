import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AddProductRequest, Order, OrderProduct, Payment, RegisterPaymentRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  createFromReservation(reservationId: number): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/reservations/${reservationId}/order`, {});
  }

  // Recupera la orden ya creada para una reserva (checkout recargado a mitad de camino,
  // doble submit, etc.) — se usa cuando createFromReservation responde 409.
  findByReservation(reservationId: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/reservations/${reservationId}/order`);
  }

  findById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/orders/${id}`);
  }

  findProducts(id: number): Observable<OrderProduct[]> {
    return this.http.get<OrderProduct[]>(`${this.apiUrl}/orders/${id}/products`);
  }

  addProduct(id: number, request: AddProductRequest): Observable<OrderProduct> {
    return this.http.post<OrderProduct>(`${this.apiUrl}/orders/${id}/products`, request);
  }

  registerPayment(id: number, request: RegisterPaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/orders/${id}/payments`, request);
  }

  cancel(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/orders/${id}/cancel`, {});
  }
}
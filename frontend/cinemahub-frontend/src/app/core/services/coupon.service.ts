import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Coupon, GenerateCouponRequest } from '../models/promotion.model';

@Injectable({ providedIn: 'root' })
export class CouponService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  findByPromotion(promotionId: number): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/promotions/${promotionId}/coupons`);
  }

  generate(promotionId: number, request: GenerateCouponRequest): Observable<Coupon> {
    return this.http.post<Coupon>(`${this.apiUrl}/promotions/${promotionId}/coupons`, request);
  }

  // Valida que el cupón exista y no esté vencido (el backend responde 404/400 si no es válido).
  validate(code: string): Observable<Coupon> {
    return this.http.get<Coupon>(`${this.apiUrl}/coupons/${code}/validate`);
  }
}
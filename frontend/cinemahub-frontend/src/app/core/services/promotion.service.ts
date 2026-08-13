import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ChangePromotionStatusRequest, CreatePromotionRequest, Promotion, PromotionStatus } from '../models/promotion.model';

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/promotions`;

  // El backend solo expone findByStatus; por defecto lista las ACTIVE.
  findByStatus(status: PromotionStatus = 'ACTIVE'): Observable<Promotion[]> {
    const params = new HttpParams().set('status', status);
    return this.http.get<Promotion[]>(this.baseUrl, { params });
  }

  findById(id: number): Observable<Promotion> {
    return this.http.get<Promotion>(`${this.baseUrl}/${id}`);
  }

  create(request: CreatePromotionRequest): Observable<Promotion> {
    return this.http.post<Promotion>(this.baseUrl, request);
  }

  changeStatus(id: number, request: ChangePromotionStatusRequest): Observable<Promotion> {
    return this.http.patch<Promotion>(`${this.baseUrl}/${id}/status`, request);
  }
}
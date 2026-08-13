import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AdjustStockRequest,
  CreateProductRequest,
  InventoryMovement,
  Product,
  ProductStatus
} from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/products`;

  // Requiere categoryId o status (igual que el backend: no hay un findAll sin filtros).
  search(filters: { categoryId?: number; status?: ProductStatus }): Observable<Product[]> {
    let params = new HttpParams();
    if (filters.categoryId != null) {
      params = params.set('categoryId', filters.categoryId);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    return this.http.get<Product[]>(this.baseUrl, { params });
  }

  findById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  adjustStock(id: number, request: AdjustStockRequest): Observable<InventoryMovement> {
    return this.http.post<InventoryMovement>(`${this.baseUrl}/${id}/stock`, request);
  }

  findMovements(id: number): Observable<InventoryMovement[]> {
    return this.http.get<InventoryMovement[]>(`${this.baseUrl}/${id}/movements`);
  }
}
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ProductCategory, ProductCategoryRequest } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductCategoryService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/product-categories`;

  findAll(): Observable<ProductCategory[]> {
    return this.http.get<ProductCategory[]>(this.baseUrl);
  }

  findById(id: number): Observable<ProductCategory> {
    return this.http.get<ProductCategory>(`${this.baseUrl}/${id}`);
  }

  create(request: ProductCategoryRequest): Observable<ProductCategory> {
    return this.http.post<ProductCategory>(this.baseUrl, request);
  }

  update(id: number, request: ProductCategoryRequest): Observable<ProductCategory> {
    return this.http.put<ProductCategory>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
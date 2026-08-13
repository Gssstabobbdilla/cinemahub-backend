import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateNotificationRequest, Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  create(request: CreateNotificationRequest): Observable<Notification> {
    return this.http.post<Notification>(`${this.apiUrl}/notifications`, request);
  }

  findByUser(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/users/${userId}/notifications`);
  }

  findUnread(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/users/${userId}/notifications/unread`);
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.patch<Notification>(`${this.apiUrl}/notifications/${id}/read`, {});
  }

  markAllAsRead(userId: number): Observable<{ updated: number }> {
    return this.http.patch<{ updated: number }>(`${this.apiUrl}/users/${userId}/notifications/read-all`, {});
  }
}
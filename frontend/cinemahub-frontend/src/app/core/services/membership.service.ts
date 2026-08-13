import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AdjustPointsRequest, ChangeLevelRequest, Membership, PointHistory } from '../models/membership.model';

@Injectable({ providedIn: 'root' })
export class MembershipService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  createForUser(userId: number): Observable<Membership> {
    return this.http.post<Membership>(`${this.apiUrl}/users/${userId}/membership`, {});
  }

  findByUser(userId: number): Observable<Membership> {
    return this.http.get<Membership>(`${this.apiUrl}/users/${userId}/membership`);
  }

  findById(id: number): Observable<Membership> {
    return this.http.get<Membership>(`${this.apiUrl}/memberships/${id}`);
  }

  adjustPoints(id: number, request: AdjustPointsRequest): Observable<Membership> {
    return this.http.post<Membership>(`${this.apiUrl}/memberships/${id}/points`, request);
  }

  changeLevel(id: number, request: ChangeLevelRequest): Observable<Membership> {
    return this.http.patch<Membership>(`${this.apiUrl}/memberships/${id}/level`, request);
  }

  findHistory(id: number): Observable<PointHistory[]> {
    return this.http.get<PointHistory[]>(`${this.apiUrl}/memberships/${id}/history`);
  }
}
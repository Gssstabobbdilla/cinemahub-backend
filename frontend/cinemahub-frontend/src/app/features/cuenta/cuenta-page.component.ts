import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Membership } from '../../core/models/membership.model';
import { Notification } from '../../core/models/notification.model';
import {
  Reservation,
  ReservationSeat
} from '../../core/models/reservation.model';
import { User } from '../../core/models/security.model';

import { MembershipService } from '../../core/services/membership.service';
import { NotificationService } from '../../core/services/notification.service';
import { ReservationService } from '../../core/services/reservation.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-cuenta-page',
  standalone: true,
  templateUrl: './cuenta-page.component.html',
  styleUrl: './cuenta-page.component.scss'
})
export class CuentaPageComponent implements OnInit {
  private userService = inject(UserService);
  private reservationService = inject(ReservationService);
  private membershipService = inject(MembershipService);
  private notificationService = inject(NotificationService);

  userIdInput = signal<number | null>(null);
  userId = signal<number | null>(null);

  user = signal<User | null>(null);
  reservations = signal<Reservation[]>([]);
  membership = signal<Membership | null>(null);
  membershipHistory = signal<any[]>([]);
  notifications = signal<Notification[]>([]);

  loading = signal(false);
  error = signal<string | null>(null);

  editingProfile = signal(false);
  savingProfile = signal(false);

  profileFirstName = signal('');
  profileLastName = signal('');
  profilePhone = signal('');

  expandedReservationId = signal<number | null>(null);
  reservationSeats = signal<Record<number, ReservationSeat[]>>({});

  showMembershipHistory = signal(false);
  membershipNotFound = signal(false);
  creatingMembership = signal(false);

  showOnlyUnread = signal(false);

  ngOnInit(): void {}

  onUserIdInputChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;

    this.userIdInput.set(Number.isNaN(value) ? null : value);
  }

  loadAccount(): void {
    const userId = this.userIdInput();

    if (!userId) {
      this.error.set('Ingresa un ID de usuario válido.');
      return;
    }

    this.userId.set(userId);
    this.loading.set(true);
    this.error.set(null);
    this.membershipNotFound.set(false);

    this.userService.findById(userId).subscribe({
      next: user => {
        this.user.set(user);
        this.profileFirstName.set(user.firstName);
        this.profileLastName.set(user.lastName);
        this.profilePhone.set(user.phone ?? '');
      },
      error: (err: AppError) => this.error.set(err.message)
    });

    this.reservationService.findByUser(userId).subscribe({
      next: reservations => this.reservations.set(reservations),
      error: (err: AppError) => this.error.set(err.message)
    });

    this.membershipService.findByUser(userId).subscribe({
      next: membership => {
        this.membership.set(membership);
        this.membershipNotFound.set(false);
      },
      error: (err: AppError) => {
        if ((err as any).status === 404) {
          this.membership.set(null);
          this.membershipNotFound.set(true);
          return;
        }

        this.error.set(err.message);
      }
    });

    this.notificationService.findByUser(userId).subscribe({
      next: notifications => this.notifications.set(notifications),
      error: (err: AppError) => this.error.set(err.message)
    });

    this.loading.set(false);
  }

  startEditProfile(): void {
    const user = this.user();

    if (!user) {
      return;
    }

    this.profileFirstName.set(user.firstName);
    this.profileLastName.set(user.lastName);
    this.profilePhone.set(user.phone ?? '');

    this.editingProfile.set(true);
  }

  onFirstNameChange(event: Event): void {
    this.profileFirstName.set(
      (event.target as HTMLInputElement).value
    );
  }

  onLastNameChange(event: Event): void {
    this.profileLastName.set(
      (event.target as HTMLInputElement).value
    );
  }

  onPhoneChange(event: Event): void {
    this.profilePhone.set(
      (event.target as HTMLInputElement).value
    );
  }

  saveProfile(): void {
    const userId = this.userId();

    if (!userId) {
      return;
    }

    this.savingProfile.set(true);
    this.error.set(null);

    this.userService
      .updateProfile(userId, {
        firstName: this.profileFirstName(),
        lastName: this.profileLastName(),
        phone: this.profilePhone()
      })
      .subscribe({
        next: user => {
          this.user.set(user);
          this.editingProfile.set(false);
          this.savingProfile.set(false);
        },
        error: (err: AppError) => {
          this.error.set(err.message);
          this.savingProfile.set(false);
        }
      });
  }

  toggleReservationSeats(reservationId: number): void {
    if (this.expandedReservationId() === reservationId) {
      this.expandedReservationId.set(null);
      return;
    }

    this.expandedReservationId.set(reservationId);

    if (this.reservationSeats()[reservationId]) {
      return;
    }

    this.reservationService.findSeats(reservationId).subscribe({
      next: seats => {
        this.reservationSeats.update(current => ({
          ...current,
          [reservationId]: seats
        }));
      },
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  getSeatsFor(reservationId: number): ReservationSeat[] {
    return this.reservationSeats()[reservationId] ?? [];
  }

  toggleMembershipHistory(): void {
    const visible = !this.showMembershipHistory();

    this.showMembershipHistory.set(visible);

    if (!visible) {
      return;
    }

    const userId = this.userId();

    if (!userId) {
      return;
    }

    this.membershipService.findHistory(userId).subscribe({
      next: history => this.membershipHistory.set(history),
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  createMembership(): void {
    const userId = this.userId();

    if (!userId) {
      return;
    }

    this.creatingMembership.set(true);
    this.error.set(null);

    this.membershipService.createForUser(userId).subscribe({
      next: membership => {
        this.membership.set(membership);
        this.membershipNotFound.set(false);
        this.creatingMembership.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.creatingMembership.set(false);
      }
    });
  }

  toggleUnreadOnly(): void {
    const onlyUnread = !this.showOnlyUnread();

    this.showOnlyUnread.set(onlyUnread);

    const userId = this.userId();

    if (!userId) {
      return;
    }

    const request = onlyUnread
      ? this.notificationService.findUnread(userId)
      : this.notificationService.findByUser(userId);

    request.subscribe({
      next: notifications => this.notifications.set(notifications),
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  markAsRead(notificationId: number): void {
    this.notificationService.markAsRead(notificationId).subscribe({
      next: updated => {
        this.notifications.update(notifications =>
          notifications.map(notification =>
            notification.id === notificationId
              ? updated
              : notification
          )
        );
      },
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  markAllAsRead(): void {
    const userId = this.userId();

    if (!userId) {
      return;
    }

    this.notificationService.markAllAsRead(userId).subscribe({
      next: () => {
        this.notifications.update(notifications =>
          notifications.map(notification => ({
            ...notification,
            read: true
          }))
        );
      },
      error: (err: AppError) => this.error.set(err.message)
    });
  }
}

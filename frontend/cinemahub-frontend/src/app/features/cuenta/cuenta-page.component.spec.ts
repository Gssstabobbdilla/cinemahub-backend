import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { describe, beforeEach, it, expect, vi } from 'vitest';

import { Membership } from '../../core/models/membership.model';
import { Notification } from '../../core/models/notification.model';
import {
  Reservation,
  ReservationSeat
} from '../../core/models/reservation.model';
import { User } from '../../core/models/security.model';

import { CurrentUserService } from '../../core/services/current-user.service';
import { MembershipService } from '../../core/services/membership.service';
import { NotificationService } from '../../core/services/notification.service';
import { ReservationService } from '../../core/services/reservation.service';
import { UserService } from '../../core/services/user.service';

import { CuentaPageComponent } from './cuenta-page.component';

describe('CuentaPageComponent', () => {
  let fixture: ComponentFixture<CuentaPageComponent>;
  let currentUserService: CurrentUserService;

  let userServiceSpy: {
    findById: ReturnType<typeof vi.fn>;
    updateProfile: ReturnType<typeof vi.fn>;
  };

  let reservationServiceSpy: {
    findByUser: ReturnType<typeof vi.fn>;
    findSeats: ReturnType<typeof vi.fn>;
  };

  let membershipServiceSpy: {
    findByUser: ReturnType<typeof vi.fn>;
    createForUser: ReturnType<typeof vi.fn>;
    findHistory: ReturnType<typeof vi.fn>;
  };

  let notificationServiceSpy: {
    findByUser: ReturnType<typeof vi.fn>;
    findUnread: ReturnType<typeof vi.fn>;
    markAsRead: ReturnType<typeof vi.fn>;
    markAllAsRead: ReturnType<typeof vi.fn>;
  };

  const user: User = {
    id: 1,
    firstName: 'Ana',
    lastName: 'Test',
    email: 'ana@cinemahub.local',
    phone: null,
    birthDate: null,
    status: 'ACTIVE',
    createdAt: 'x'
  };

  const reservation: Reservation = {
    id: 50,
    userId: 1,
    status: 'CONFIRMED',
    expiresAt: 'x',
    createdAt: 'x'
  };

  const membership: Membership = {
    id: 10,
    userId: 1,
    level: 'BASIC',
    points: 0
  };

  const notification: Notification = {
    id: 3,
    userId: 1,
    title: 'Hola',
    message: null,
    read: false,
    createdAt: 'x'
  };

  beforeEach(() => {
    userServiceSpy = {
      findById: vi.fn(),
      updateProfile: vi.fn()
    };

    reservationServiceSpy = {
      findByUser: vi.fn(),
      findSeats: vi.fn()
    };

    membershipServiceSpy = {
      findByUser: vi.fn(),
      createForUser: vi.fn(),
      findHistory: vi.fn()
    };

    notificationServiceSpy = {
      findByUser: vi.fn(),
      findUnread: vi.fn(),
      markAsRead: vi.fn(),
      markAllAsRead: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [CuentaPageComponent],
      providers: [
        {
          provide: UserService,
          useValue: userServiceSpy
        },
        {
          provide: ReservationService,
          useValue: reservationServiceSpy
        },
        {
          provide: MembershipService,
          useValue: membershipServiceSpy
        },
        {
          provide: NotificationService,
          useValue: notificationServiceSpy
        }
        // CurrentUserService no se mockea: es un signal simple sin HTTP,
        // se usa la instancia real provista via providedIn: 'root'.
      ]
    });

    currentUserService = TestBed.inject(CurrentUserService);
    fixture = TestBed.createComponent(CuentaPageComponent);
  });

  it('loadAccount trae perfil, reservas, membresía y notificaciones del usuario', () => {
    userServiceSpy.findById.mockReturnValue(of(user));
    reservationServiceSpy.findByUser.mockReturnValue(of([reservation]));
    membershipServiceSpy.findByUser.mockReturnValue(of(membership));
    notificationServiceSpy.findByUser.mockReturnValue(of([notification]));

    fixture.componentInstance.userIdInput.set(1);
    fixture.componentInstance.loadAccount();

    expect(userServiceSpy.findById).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.user()).toEqual(user);
    expect(fixture.componentInstance.reservations()).toEqual([reservation]);
    expect(fixture.componentInstance.membership()).toEqual(membership);
    expect(fixture.componentInstance.notifications()).toEqual([notification]);

    // loadAccount debe compartir el userId con CurrentUserService (para que
    // reservas lo herede si el usuario navega ahí después).
    expect(currentUserService.userId()).toBe(1);
  });

  it('si el usuario no tiene membresía (404), setea membershipNotFound', () => {
    userServiceSpy.findById.mockReturnValue(of(user));
    reservationServiceSpy.findByUser.mockReturnValue(of([]));
    membershipServiceSpy.findByUser.mockReturnValue(
      throwError(() => ({
        status: 404,
        message: 'no tiene'
      }))
    );
    notificationServiceSpy.findByUser.mockReturnValue(of([]));

    fixture.componentInstance.userIdInput.set(1);
    fixture.componentInstance.loadAccount();

    expect(fixture.componentInstance.membership()).toBeNull();
    expect(fixture.componentInstance.membershipNotFound()).toBe(true);
  });

  it('createMembership crea la membresía y limpia membershipNotFound', () => {
    currentUserService.setUserId(1);

    membershipServiceSpy.createForUser.mockReturnValue(of(membership));

    fixture.componentInstance.createMembership();

    expect(membershipServiceSpy.createForUser).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.membership()).toEqual(membership);
    expect(fixture.componentInstance.membershipNotFound()).toBe(false);
  });

  it('toggleReservationSeats carga los asientos la primera vez y los cachea después', () => {
    const seats: ReservationSeat[] = [
      {
        seatId: 1,
        rowLabel: 'A',
        seatNumber: 1,
        price: 25
      }
    ];

    reservationServiceSpy.findSeats.mockReturnValue(of(seats));

    fixture.componentInstance.toggleReservationSeats(50);

    expect(reservationServiceSpy.findSeats).toHaveBeenCalledWith(50);
    expect(fixture.componentInstance.getSeatsFor(50)).toEqual(seats);

    fixture.componentInstance.toggleReservationSeats(50);

    expect(
      fixture.componentInstance.expandedReservationId()
    ).toBeNull();

    fixture.componentInstance.toggleReservationSeats(50);

    expect(reservationServiceSpy.findSeats).toHaveBeenCalledTimes(1);
  });

  it('markAsRead actualiza esa notificación en la lista sin recargar todo', () => {
    fixture.componentInstance.notifications.set([notification]);

    const updated = {
      ...notification,
      read: true
    };

    notificationServiceSpy.markAsRead.mockReturnValue(of(updated));

    fixture.componentInstance.markAsRead(3);

    expect(fixture.componentInstance.notifications()).toEqual([
      updated
    ]);
  });

  it('saveProfile guarda los cambios y sale del modo edición', () => {
    currentUserService.setUserId(1);
    fixture.componentInstance.profileFirstName.set('Ana');
    fixture.componentInstance.profileLastName.set('Editada');
    fixture.componentInstance.profilePhone.set('999999999');
    fixture.componentInstance.editingProfile.set(true);

    const updated = {
      ...user,
      lastName: 'Editada',
      phone: '999999999'
    };

    userServiceSpy.updateProfile.mockReturnValue(of(updated));

    fixture.componentInstance.saveProfile();

    expect(userServiceSpy.updateProfile).toHaveBeenCalledWith(1, {
      firstName: 'Ana',
      lastName: 'Editada',
      phone: '999999999'
    });

    expect(fixture.componentInstance.user()).toEqual(updated);
    expect(fixture.componentInstance.editingProfile()).toBe(false);
  });

  it('ngOnInit no autocarga la cuenta si CurrentUserService no tiene userId', () => {
    fixture.componentInstance.ngOnInit();

    expect(userServiceSpy.findById).not.toHaveBeenCalled();
    expect(fixture.componentInstance.userIdInput()).toBeNull();
  });

  it('ngOnInit precarga y carga la cuenta si CurrentUserService ya tenía un userId (ej. viniendo de reservas)', () => {
    currentUserService.setUserId(1);
    userServiceSpy.findById.mockReturnValue(of(user));
    reservationServiceSpy.findByUser.mockReturnValue(of([]));
    membershipServiceSpy.findByUser.mockReturnValue(of(membership));
    notificationServiceSpy.findByUser.mockReturnValue(of([]));

    fixture.componentInstance.ngOnInit();

    expect(fixture.componentInstance.userIdInput()).toBe(1);
    expect(userServiceSpy.findById).toHaveBeenCalledWith(1);
  });
});
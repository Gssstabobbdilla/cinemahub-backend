import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Seat } from '../../core/models/cinema.model';
import { Showtime } from '../../core/models/showtime.model';
import { ReservationService } from '../../core/services/reservation.service';
import { ShowtimeService } from '../../core/services/showtime.service';
import { SeatMapComponent } from '../../shared/components/seat-map/seat-map.component';

@Component({
  selector: 'app-reservas-page',
  standalone: true,
  imports: [SeatMapComponent],
  templateUrl: './reservas-page.component.html',
  styleUrl: './reservas-page.component.scss'
})
export class ReservasPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private showtimeService = inject(ShowtimeService);
  private reservationService = inject(ReservationService);

  showtime = signal<Showtime | null>(null);
  seats = signal<Seat[]>([]);
  takenSeatIds = signal<number[]>([]);
  selectedSeatIds = signal<number[]>([]);

  loading = signal(true);
  error = signal<string | null>(null);
  submitting = signal(false);

  // TODO: reemplazar por el id del usuario autenticado cuando exista login real
  // (el authInterceptor y TokenStorageService ya están listos para ese momento).
  userId = signal<number | null>(null);

  total = computed(() => {
    const showtime = this.showtime();
    return showtime ? this.selectedSeatIds().length * showtime.basePrice : 0;
  });

  private showtimeId!: number;

  ngOnInit(): void {
    this.showtimeId = Number(this.route.snapshot.paramMap.get('showtimeId'));

    this.showtimeService.findById(this.showtimeId).subscribe({
      next: showtime => this.showtime.set(showtime),
      error: (err: AppError) => this.error.set(err.message)
    });

    this.reservationService.findShowtimeSeats(this.showtimeId).subscribe({
      next: showtimeSeats => {
        // roomId no lo usa SeatMapComponent (solo agrupa por rowLabel); se completa con 0
        // porque ShowtimeSeatResponse del backend no lo trae.
        this.seats.set(
          showtimeSeats.map(s => ({
            id: s.seatId,
            roomId: 0,
            rowLabel: s.rowLabel,
            seatNumber: s.seatNumber,
            seatType: s.seatType
          }))
        );
        this.takenSeatIds.set(showtimeSeats.filter(s => s.taken).map(s => s.seatId));
        this.loading.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  onUserIdChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.userId.set(Number.isNaN(value) ? null : value);
  }

  onSelectionChange(seatIds: number[]): void {
    this.selectedSeatIds.set(seatIds);
  }

  confirmReservation(): void {
    const userId = this.userId();
    if (!userId || this.selectedSeatIds().length === 0) {
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    this.reservationService
      .create({ userId, showtimeId: this.showtimeId, seatIds: this.selectedSeatIds() })
      .subscribe({
        next: reservation => this.router.navigate(['/checkout', reservation.id]),
        error: (err: AppError) => {
          // Acá es donde llega, por ejemplo, el 409 de doble reserva que tanto probamos
          // en el backend — err.message ya viene humano-legible gracias al interceptor.
          this.error.set(err.message);
          this.submitting.set(false);
        }
      });
  }
}
import { Component, computed, input, output } from '@angular/core';

import { Seat } from '../../../core/models/cinema.model';

interface SeatRow {
  rowLabel: string;
  seats: Seat[];
}

// Componente presentacional puro: no sabe nada de reservas ni de HTTP. El componente
// que lo use (la feature reservas) es responsable de resolver takenSeatIds — hoy el
// backend no tiene un endpoint de disponibilidad por función, hace falta agregarlo.
@Component({
  selector: 'app-seat-map',
  standalone: true,
  templateUrl: './seat-map.component.html',
  styleUrl: './seat-map.component.scss'
})
export class SeatMapComponent {
  seats = input.required<Seat[]>();
  takenSeatIds = input<number[]>([]);
  selectedSeatIds = input<number[]>([]);

  selectionChange = output<number[]>();

  rows = computed<SeatRow[]>(() => {
    const grouped = new Map<string, Seat[]>();
    for (const seat of this.seats()) {
      const list = grouped.get(seat.rowLabel) ?? [];
      list.push(seat);
      grouped.set(seat.rowLabel, list);
    }
    return Array.from(grouped.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([rowLabel, rowSeats]) => ({
        rowLabel,
        seats: [...rowSeats].sort((a, b) => a.seatNumber - b.seatNumber)
      }));
  });

  isTaken(seat: Seat): boolean {
    return this.takenSeatIds().includes(seat.id);
  }

  isSelected(seat: Seat): boolean {
    return this.selectedSeatIds().includes(seat.id);
  }

  toggle(seat: Seat): void {
    if (this.isTaken(seat)) {
      return;
    }
    const current = this.selectedSeatIds();
    const next = this.isSelected(seat)
      ? current.filter(id => id !== seat.id)
      : [...current, seat.id];
    this.selectionChange.emit(next);
  }
}
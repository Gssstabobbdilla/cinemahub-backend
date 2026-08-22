import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Cinema, Room, Seat } from '../../core/models/cinema.model';
import { CinemaService } from '../../core/services/cinema.service';
import { RoomService } from '../../core/services/room.service';
import { SeatService } from '../../core/services/seat.service';

@Component({
  selector: 'app-cines-page',
  standalone: true,
  templateUrl: './cines-page.component.html',
  styleUrl: './cines-page.component.scss'
})
export class CinesPageComponent implements OnInit {
  private cinemaService = inject(CinemaService);
  private roomService = inject(RoomService);
  private seatService = inject(SeatService);

  cinemas = signal<Cinema[]>([]);
  selectedCinema = signal<Cinema | null>(null);
  rooms = signal<Room[]>([]);
  seatCounts = signal<Record<number, number>>({});

  loading = signal(true);
  loadingRooms = signal(false);
  error = signal<string | null>(null);

  // --- form de cine ---
  showCinemaForm = signal(false);
  editingCinema = signal<Cinema | null>(null);
  savingCinema = signal(false);

  formName = signal('');
  formDepartment = signal('');
  formProvince = signal('');
  formDistrict = signal('');
  formAddress = signal('');

  // --- form de sala ---
  showRoomForm = signal(false);
  savingRoom = signal(false);
  roomName = signal('');
  roomCapacity = signal<number | null>(null);

  // --- generación de asientos ---
  generatingSeatsFor = signal<number | null>(null);
  seatRowCount = signal<number | null>(6);
  seatsPerRow = signal<number | null>(10);
  generatingSeats = signal(false);

  ngOnInit(): void {
    this.loadCinemas();
  }

  private loadCinemas(): void {
    this.loading.set(true);
    this.cinemaService.findAll().subscribe({
      next: cinemas => {
        this.cinemas.set(cinemas);
        this.loading.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  selectCinema(cinema: Cinema): void {
    this.selectedCinema.set(cinema);
    this.loadRooms(cinema.id);
  }

  private loadRooms(cinemaId: number): void {
    this.loadingRooms.set(true);
    this.roomService.findByCinema(cinemaId).subscribe({
      next: rooms => {
        this.rooms.set(rooms);
        this.loadingRooms.set(false);
        rooms.forEach(r => this.loadSeatCount(r.id));
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loadingRooms.set(false);
      }
    });
  }

  private loadSeatCount(roomId: number): void {
    this.seatService.findByRoom(roomId).subscribe({
      next: seats => {
        this.seatCounts.update(current => ({ ...current, [roomId]: seats.length }));
      }
    });
  }

  seatCountFor(roomId: number): number {
    return this.seatCounts()[roomId] ?? 0;
  }

  // --- cine: crear/editar ---
  openCreateCinemaForm(): void {
    this.editingCinema.set(null);
    this.formName.set('');
    this.formDepartment.set('');
    this.formProvince.set('');
    this.formDistrict.set('');
    this.formAddress.set('');
    this.showCinemaForm.set(true);
  }

  openEditCinemaForm(cinema: Cinema): void {
    this.editingCinema.set(cinema);
    this.formName.set(cinema.name);
    this.formDepartment.set(cinema.department ?? '');
    this.formProvince.set(cinema.province ?? '');
    this.formDistrict.set(cinema.district ?? '');
    this.formAddress.set(cinema.address ?? '');
    this.showCinemaForm.set(true);
  }

  closeCinemaForm(): void {
    this.showCinemaForm.set(false);
    this.error.set(null);
  }

  onNameChange(event: Event): void {
    this.formName.set((event.target as HTMLInputElement).value);
  }

  onDepartmentChange(event: Event): void {
    this.formDepartment.set((event.target as HTMLInputElement).value);
  }

  onProvinceChange(event: Event): void {
    this.formProvince.set((event.target as HTMLInputElement).value);
  }

  onDistrictChange(event: Event): void {
    this.formDistrict.set((event.target as HTMLInputElement).value);
  }

  onAddressChange(event: Event): void {
    this.formAddress.set((event.target as HTMLInputElement).value);
  }

  saveCinema(): void {
    const name = this.formName().trim();
    if (!name) {
      this.error.set('El nombre del cine es obligatorio.');
      return;
    }

    this.savingCinema.set(true);
    this.error.set(null);

    const editing = this.editingCinema();
    const request = {
      name,
      department: this.formDepartment() || undefined,
      province: this.formProvince() || undefined,
      district: this.formDistrict() || undefined,
      address: this.formAddress() || undefined
    };

    const request$ = editing
      ? this.cinemaService.update(editing.id, request)
      : this.cinemaService.create({ name });

    request$.subscribe({
      next: () => {
        this.savingCinema.set(false);
        this.showCinemaForm.set(false);
        this.loadCinemas();
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.savingCinema.set(false);
      }
    });
  }

  deleteCinema(cinema: Cinema): void {
    this.cinemaService.delete(cinema.id).subscribe({
      next: () => {
        if (this.selectedCinema()?.id === cinema.id) {
          this.selectedCinema.set(null);
          this.rooms.set([]);
        }
        this.loadCinemas();
      },
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  // --- sala: crear/eliminar ---
  openCreateRoomForm(): void {
    this.roomName.set('');
    this.roomCapacity.set(null);
    this.showRoomForm.set(true);
  }

  closeRoomForm(): void {
    this.showRoomForm.set(false);
  }

  onRoomNameChange(event: Event): void {
    this.roomName.set((event.target as HTMLInputElement).value);
  }

  onRoomCapacityChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.roomCapacity.set(Number.isNaN(value) ? null : value);
  }

  saveRoom(): void {
    const cinema = this.selectedCinema();
    const name = this.roomName().trim();
    const capacity = this.roomCapacity();

    if (!cinema || !name || !capacity) {
      this.error.set('Completa nombre y capacidad de la sala.');
      return;
    }

    this.savingRoom.set(true);
    this.error.set(null);

    this.roomService.create(cinema.id, { name, capacity }).subscribe({
      next: () => {
        this.savingRoom.set(false);
        this.showRoomForm.set(false);
        this.loadRooms(cinema.id);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.savingRoom.set(false);
      }
    });
  }

  deleteRoom(room: Room): void {
    const cinema = this.selectedCinema();
    if (!cinema) {
      return;
    }
    this.roomService.delete(room.id).subscribe({
      next: () => this.loadRooms(cinema.id),
      error: (err: AppError) => this.error.set(err.message)
    });
  }

  // --- asientos ---
  openSeatGenerator(room: Room): void {
    this.generatingSeatsFor.set(room.id);
    this.seatRowCount.set(6);
    this.seatsPerRow.set(10);
  }

  closeSeatGenerator(): void {
    this.generatingSeatsFor.set(null);
  }

  onRowCountChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.seatRowCount.set(Number.isNaN(value) ? null : value);
  }

  onSeatsPerRowChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.seatsPerRow.set(Number.isNaN(value) ? null : value);
  }

  confirmGenerateSeats(roomId: number): void {
    const rowCount = this.seatRowCount();
    const seatsPerRow = this.seatsPerRow();

    if (!rowCount || !seatsPerRow) {
      this.error.set('Completa filas y asientos por fila.');
      return;
    }

    this.generatingSeats.set(true);
    this.error.set(null);

    this.seatService.generateSeats(roomId, { rowCount, seatsPerRow }).subscribe({
      next: (seats: Seat[]) => {
        this.seatCounts.update(current => ({ ...current, [roomId]: seats.length }));
        this.generatingSeats.set(false);
        this.generatingSeatsFor.set(null);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.generatingSeats.set(false);
      }
    });
  }
}
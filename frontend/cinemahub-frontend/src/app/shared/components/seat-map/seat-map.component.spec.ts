import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Seat } from '../../../core/models/cinema.model';
import { SeatMapComponent } from './seat-map.component';

describe('SeatMapComponent', () => {
  let fixture: ComponentFixture<SeatMapComponent>;
  let component: SeatMapComponent;

  const seats: Seat[] = [
    { id: 1, roomId: 1, rowLabel: 'A', seatNumber: 2, seatType: 'STANDARD' },
    { id: 2, roomId: 1, rowLabel: 'A', seatNumber: 1, seatType: 'STANDARD' },
    { id: 3, roomId: 1, rowLabel: 'B', seatNumber: 1, seatType: 'STANDARD' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [SeatMapComponent] });
    fixture = TestBed.createComponent(SeatMapComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('seats', seats);
    fixture.detectChanges();
  });

  it('agrupa los asientos por fila (ordenadas) y los ordena por número dentro de cada fila', () => {
    const rows = component.rows();
    expect(rows.map(r => r.rowLabel)).toEqual(['A', 'B']);
    expect(rows[0].seats.map(s => s.seatNumber)).toEqual([1, 2]);
  });

  it('isTaken devuelve true solo para los IDs incluidos en takenSeatIds', () => {
    fixture.componentRef.setInput('takenSeatIds', [1]);
    fixture.detectChanges();

    expect(component.isTaken(seats[0])).toBe(true);
    expect(component.isTaken(seats[1])).toBe(false);
  });

  it('toggle agrega un asiento disponible a la selección', () => {
    let emitted: number[] | undefined;
    component.selectionChange.subscribe(ids => (emitted = ids));

    component.toggle(seats[1]); // id 2, disponible
    expect(emitted).toEqual([2]);
  });

  it('toggle quita un asiento que ya estaba seleccionado', () => {
    fixture.componentRef.setInput('selectedSeatIds', [2]);
    fixture.detectChanges();

    let emitted: number[] | undefined;
    component.selectionChange.subscribe(ids => (emitted = ids));

    component.toggle(seats[1]);
    expect(emitted).toEqual([]);
  });

  it('toggle no emite nada sobre un asiento ocupado', () => {
    fixture.componentRef.setInput('takenSeatIds', [1]);
    fixture.detectChanges();

    let emitted: number[] | undefined;
    component.selectionChange.subscribe(ids => (emitted = ids));

    component.toggle(seats[0]);
    expect(emitted).toBeUndefined();
  });
});
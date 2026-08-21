import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AdminPageComponent } from './admin-page.component';

describe('AdminPageComponent', () => {
  let fixture: ComponentFixture<AdminPageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AdminPageComponent],
      providers: [provideRouter([])]
    });

    fixture = TestBed.createComponent(AdminPageComponent);
  });

  it('expone las 5 secciones de administración con path, label e icon', () => {
    fixture.detectChanges();

    const sections = fixture.componentInstance.sections;

    expect(sections).toHaveLength(5);
    expect(sections.map(s => s.path)).toEqual([
      'productos',
      'peliculas',
      'cines',
      'funciones',
      'promociones'
    ]);
    expect(sections.every(s => s.label && s.icon)).toBe(true);
  });

  it('renderiza un link de navegación por cada sección', () => {
    fixture.detectChanges();

    const links = fixture.nativeElement.querySelectorAll('.nav-item');
    expect(links.length).toBe(5);
  });

  it('renderiza el router-outlet dentro de admin-content', () => {
    fixture.detectChanges();

    const outlet = fixture.nativeElement.querySelector('.admin-content router-outlet');
    expect(outlet).toBeTruthy();
  });
});
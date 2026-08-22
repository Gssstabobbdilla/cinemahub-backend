import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Coupon, Promotion } from '../../core/models/promotion.model';
import { CouponService } from '../../core/services/coupon.service';
import { PromotionService } from '../../core/services/promotion.service';

import { PromocionesPageComponent } from './promociones-page.component';

describe('PromocionesPageComponent', () => {
  let fixture: ComponentFixture<PromocionesPageComponent>;

  let promotionServiceSpy: {
    findByStatus: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    changeStatus: ReturnType<typeof vi.fn>;
  };

  let couponServiceSpy: {
    findByPromotion: ReturnType<typeof vi.fn>;
    generate: ReturnType<typeof vi.fn>;
  };

  const promotion: Promotion = {
    id: 1,
    title: '2x1 en miércoles',
    description: null,
    discountPercentage: 50,
    startDate: '2026-01-01',
    endDate: '2026-12-31',
    status: 'ACTIVE'
  };

  const coupon: Coupon = {
    id: 5,
    promotionId: 1,
    code: 'MIERCOLES50',
    discountPercentage: 50,
    expiresAt: '2026-12-31T23:59:59-05:00'
  };

  beforeEach(() => {
    promotionServiceSpy = {
      findByStatus: vi.fn(),
      create: vi.fn(),
      changeStatus: vi.fn()
    };

    couponServiceSpy = {
      findByPromotion: vi.fn(),
      generate: vi.fn()
    };

    promotionServiceSpy.findByStatus.mockReturnValue(of([promotion]));
    couponServiceSpy.findByPromotion.mockReturnValue(of([coupon]));

    TestBed.configureTestingModule({
      imports: [PromocionesPageComponent],
      providers: [
        { provide: PromotionService, useValue: promotionServiceSpy },
        { provide: CouponService, useValue: couponServiceSpy }
      ]
    });

    fixture = TestBed.createComponent(PromocionesPageComponent);
  });

  it('carga las promociones ACTIVE por defecto', () => {
    fixture.detectChanges();

    expect(promotionServiceSpy.findByStatus).toHaveBeenCalledWith('ACTIVE');
    expect(fixture.componentInstance.promotions()).toEqual([promotion]);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('setStatusFilter recarga con el nuevo estado y limpia la selección', () => {
    fixture.detectChanges();
    fixture.componentInstance.selectPromotion(promotion);

    fixture.componentInstance.setStatusFilter('EXPIRED');

    expect(promotionServiceSpy.findByStatus).toHaveBeenCalledWith('EXPIRED');
    expect(fixture.componentInstance.selectedPromotion()).toBeNull();
    expect(fixture.componentInstance.coupons()).toEqual([]);
  });

  it('selectPromotion carga los cupones de esa promoción', () => {
    fixture.detectChanges();

    fixture.componentInstance.selectPromotion(promotion);

    expect(couponServiceSpy.findByPromotion).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.coupons()).toEqual([coupon]);
  });

  it('isExpired devuelve true para cupones vencidos', () => {
    fixture.detectChanges();

    const expiredCoupon: Coupon = { ...coupon, expiresAt: '2020-01-01T00:00:00-05:00' };
    const validCoupon: Coupon = { ...coupon, expiresAt: '2099-01-01T00:00:00-05:00' };

    expect(fixture.componentInstance.isExpired(expiredCoupon)).toBe(true);
    expect(fixture.componentInstance.isExpired(validCoupon)).toBe(false);
  });

  it('changeStatus actualiza la promoción en la lista si sigue matcheando el filtro', () => {
    const updated: Promotion = { ...promotion, status: 'ACTIVE' };
    promotionServiceSpy.changeStatus.mockReturnValue(of(updated));

    fixture.detectChanges();
    fixture.componentInstance.changeStatus(promotion, 'ACTIVE');

    expect(promotionServiceSpy.changeStatus).toHaveBeenCalledWith(1, { status: 'ACTIVE' });
    expect(fixture.componentInstance.promotions()).toEqual([updated]);
  });

  it('changeStatus saca la promoción de la lista si ya no matchea el filtro activo', () => {
    const updated: Promotion = { ...promotion, status: 'INACTIVE' };
    promotionServiceSpy.changeStatus.mockReturnValue(of(updated));

    fixture.detectChanges();
    fixture.componentInstance.selectPromotion(promotion);

    fixture.componentInstance.changeStatus(promotion, 'INACTIVE');

    expect(fixture.componentInstance.promotions()).toEqual([]);
    expect(fixture.componentInstance.selectedPromotion()).toBeNull();
  });

  it('save valida título, descuento y fechas', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formTitle.set('');

    fixture.componentInstance.save();

    expect(fixture.componentInstance.error()).toBe('Completa título, descuento, fecha de inicio y fin.');
    expect(promotionServiceSpy.create).not.toHaveBeenCalled();
  });

  it('save crea la promoción con los campos completos', () => {
    promotionServiceSpy.create.mockReturnValue(of(promotion));

    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formTitle.set('2x1 en miércoles');
    fixture.componentInstance.formDiscountPercentage.set(50);
    fixture.componentInstance.formStartDate.set('2026-01-01');
    fixture.componentInstance.formEndDate.set('2026-12-31');

    fixture.componentInstance.save();

    expect(promotionServiceSpy.create).toHaveBeenCalledWith({
      title: '2x1 en miércoles',
      discountPercentage: 50,
      startDate: '2026-01-01',
      endDate: '2026-12-31'
    });
    expect(fixture.componentInstance.showForm()).toBe(false);
  });

  it('saveCoupon valida que haya una promoción seleccionada y campos completos', () => {
    fixture.detectChanges();
    fixture.componentInstance.openCouponForm();

    fixture.componentInstance.saveCoupon();

    expect(fixture.componentInstance.error()).toBe('Completa código, descuento y fecha de expiración del cupón.');
    expect(couponServiceSpy.generate).not.toHaveBeenCalled();
  });

  it('saveCoupon genera el cupón con la fecha formateada como OffsetDateTime de fin de día', () => {
    couponServiceSpy.generate.mockReturnValue(of(coupon));

    fixture.detectChanges();
    fixture.componentInstance.selectPromotion(promotion);
    fixture.componentInstance.openCouponForm();
    fixture.componentInstance.couponCode.set('miercoles50');
    fixture.componentInstance.couponDiscountPercentage.set(50);
    fixture.componentInstance.couponExpiresAt.set('2026-12-31');

    fixture.componentInstance.saveCoupon();

    expect(couponServiceSpy.generate).toHaveBeenCalledWith(1, {
      code: 'MIERCOLES50',
      discountPercentage: 50,
      expiresAt: '2026-12-31T23:59:59-05:00'
    });
    expect(fixture.componentInstance.showCouponForm()).toBe(false);
  });

  it('saveCoupon setea error() cuando el código ya existe (409)', () => {
    couponServiceSpy.generate.mockReturnValue(
      throwError(() => ({ status: 409, message: 'Ya existe un cupón' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.selectPromotion(promotion);
    fixture.componentInstance.openCouponForm();
    fixture.componentInstance.couponCode.set('MIERCOLES50');
    fixture.componentInstance.couponDiscountPercentage.set(50);
    fixture.componentInstance.couponExpiresAt.set('2026-12-31');

    fixture.componentInstance.saveCoupon();

    expect(fixture.componentInstance.error()).toBe('Ya existe un cupón');
    expect(fixture.componentInstance.showCouponForm()).toBe(true);
  });
});
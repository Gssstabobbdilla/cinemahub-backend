  import { Component, OnInit, inject, signal } from '@angular/core';

  import { AppError } from '../../core/interceptors/error.interceptor';
  import { Coupon, Promotion, PromotionStatus } from '../../core/models/promotion.model';
  import { CouponService } from '../../core/services/coupon.service';
  import { PromotionService } from '../../core/services/promotion.service';
  import { CommonModule } from '@angular/common';

  @Component({
    selector: 'app-promociones-page',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './promociones-page.component.html',
    styleUrl: './promociones-page.component.scss'
  })
  export class PromocionesPageComponent implements OnInit {
    private promotionService = inject(PromotionService);
    private couponService = inject(CouponService);

    statusFilter = signal<PromotionStatus>('ACTIVE');
    promotions = signal<Promotion[]>([]);
    selectedPromotion = signal<Promotion | null>(null);
    coupons = signal<Coupon[]>([]);

    loading = signal(true);
    loadingCoupons = signal(false);
    error = signal<string | null>(null);

    // --- form de nueva promoción ---
    showForm = signal(false);
    saving = signal(false);

    formTitle = signal('');
    formDiscountPercentage = signal<number | null>(null);
    formStartDate = signal('');
    formEndDate = signal('');

    // --- form de nuevo cupón ---
    showCouponForm = signal(false);
    savingCoupon = signal(false);

    couponCode = signal('');
    couponDiscountPercentage = signal<number | null>(null);
    couponExpiresAt = signal('');

    ngOnInit(): void {
      this.loadPromotions();
    }

    private loadPromotions(): void {
      this.loading.set(true);
      this.promotionService.findByStatus(this.statusFilter()).subscribe({
        next: promotions => {
          this.promotions.set(promotions);
          this.loading.set(false);
        },
        error: (err: AppError) => {
          this.error.set(err.message);
          this.loading.set(false);
        }
      });
    }

    setStatusFilter(status: PromotionStatus): void {
      this.statusFilter.set(status);
      this.selectedPromotion.set(null);
      this.coupons.set([]);
      this.loadPromotions();
    }

    selectPromotion(promotion: Promotion): void {
      this.selectedPromotion.set(promotion);
      this.loadCoupons(promotion.id);
    }

    private loadCoupons(promotionId: number): void {
      this.loadingCoupons.set(true);
      this.couponService.findByPromotion(promotionId).subscribe({
        next: coupons => {
          this.coupons.set(coupons);
          this.loadingCoupons.set(false);
        },
        error: (err: AppError) => {
          this.error.set(err.message);
          this.loadingCoupons.set(false);
        }
      });
    }

    isExpired(coupon: Coupon): boolean {
      return new Date(coupon.expiresAt).getTime() < Date.now();
    }

    changeStatus(promotion: Promotion, status: PromotionStatus): void {
      this.promotionService.changeStatus(promotion.id, { status }).subscribe({
        next: updated => {
          // Si ya no matchea el filtro activo, la sacamos de la lista visible.
          if (updated.status !== this.statusFilter()) {
            this.promotions.update(list => list.filter(p => p.id !== updated.id));
            if (this.selectedPromotion()?.id === updated.id) {
              this.selectedPromotion.set(null);
              this.coupons.set([]);
            }
            return;
          }
          this.promotions.update(list => list.map(p => (p.id === updated.id ? updated : p)));
        },
        error: (err: AppError) => this.error.set(err.message)
      });
    }

    // --- form de nueva promoción ---
    openCreateForm(): void {
      this.formTitle.set('');
      this.formDiscountPercentage.set(null);
      this.formStartDate.set('');
      this.formEndDate.set('');
      this.showForm.set(true);
    }

    closeForm(): void {
      this.showForm.set(false);
      this.error.set(null);
    }

    onTitleChange(event: Event): void {
      this.formTitle.set((event.target as HTMLInputElement).value);
    }

    onDiscountPercentageChange(event: Event): void {
      const value = (event.target as HTMLInputElement).valueAsNumber;
      this.formDiscountPercentage.set(Number.isNaN(value) ? null : value);
    }

    onStartDateChange(event: Event): void {
      this.formStartDate.set((event.target as HTMLInputElement).value);
    }

    onEndDateChange(event: Event): void {
      this.formEndDate.set((event.target as HTMLInputElement).value);
    }

    save(): void {
      const title = this.formTitle().trim();
      const discountPercentage = this.formDiscountPercentage();
      const startDate = this.formStartDate();
      const endDate = this.formEndDate();

      if (!title || discountPercentage === null || !startDate || !endDate) {
        this.error.set('Completa título, descuento, fecha de inicio y fin.');
        return;
      }

      this.saving.set(true);
      this.error.set(null);

      this.promotionService.create({ title, discountPercentage, startDate, endDate }).subscribe({
        next: () => {
          this.saving.set(false);
          this.showForm.set(false);
          this.loadPromotions();
        },
        error: (err: AppError) => {
          this.error.set(err.message);
          this.saving.set(false);
        }
      });
    }

    // --- form de nuevo cupón ---
    openCouponForm(): void {
      this.couponCode.set('');
      this.couponDiscountPercentage.set(this.selectedPromotion()?.discountPercentage ?? null);
      this.couponExpiresAt.set('');
      this.showCouponForm.set(true);
    }

    closeCouponForm(): void {
      this.showCouponForm.set(false);
    }

    onCouponCodeChange(event: Event): void {
      this.couponCode.set((event.target as HTMLInputElement).value.toUpperCase());
    }

    onCouponDiscountChange(event: Event): void {
      const value = (event.target as HTMLInputElement).valueAsNumber;
      this.couponDiscountPercentage.set(Number.isNaN(value) ? null : value);
    }

    onCouponExpiresAtChange(event: Event): void {
      this.couponExpiresAt.set((event.target as HTMLInputElement).value);
    }

    saveCoupon(): void {
      const promotion = this.selectedPromotion();
      const code = this.couponCode().trim().toUpperCase();
      const discountPercentage = this.couponDiscountPercentage(); 
      const expiresAtDate = this.couponExpiresAt();

      if (!promotion || !code || discountPercentage === null || !expiresAtDate) {
        this.error.set('Completa código, descuento y fecha de expiración del cupón.');
        return;
      }

      this.savingCoupon.set(true);
      this.error.set(null);

      // El backend espera un OffsetDateTime; el input date solo da la fecha, así que
      // fijamos la expiración al final del día seleccionado.
      const expiresAt = `${expiresAtDate}T23:59:59-05:00`;

      this.couponService.generate(promotion.id, { code, discountPercentage, expiresAt }).subscribe({
        next: () => {
          this.savingCoupon.set(false);
          this.showCouponForm.set(false);
          this.loadCoupons(promotion.id);
        },
        error: (err: AppError) => {
          //this.error.set(err.message);
          //this.savingCoupon.set(false);
          console.log('ERROR DEL BACKEND:', err);
          console.log('MESSAGE:', err.message);

          this.error.set(err.message);
          this.savingCoupon.set(false);
        }
      });
    }
  }
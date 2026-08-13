// Espejo de com.cinemahub.cinemahub.promotion.dto.* (backend)

export type PromotionStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED';

export interface Promotion {
  id: number;
  title: string;
  description: string | null;
  discountPercentage: number;
  startDate: string;
  endDate: string;
  status: PromotionStatus;
}

// Coincide con PromotionService.create(title, discountPercentage, startDate, endDate):
// description no se puede setear en la creación todavía.
export interface CreatePromotionRequest {
  title: string;
  discountPercentage: number;
  startDate: string;
  endDate: string;
}

export interface ChangePromotionStatusRequest {
  status: PromotionStatus;
}

export interface Coupon {
  id: number;
  promotionId: number;
  code: string;
  discountPercentage: number;
  expiresAt: string;
}

// El promotionId va por la URL (/promotions/{promotionId}/coupons), no en el body.
export interface GenerateCouponRequest {
  code: string;
  discountPercentage: number;
  expiresAt: string;
}
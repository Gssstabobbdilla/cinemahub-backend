// Espejo de com.cinemahub.cinemahub.order.dto.* (backend)

export type OrderStatus = 'PENDING' | 'PAID' | 'CANCELLED' | 'REFUNDED';
export type PaymentStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REFUNDED';

// Los productos de la orden NO viajan acá: se consultan aparte con
// OrderService.findProducts -> GET /api/orders/{id}/products.
export interface Order {
  id: number;
  reservationId: number;
  total: number;
  status: OrderStatus;
  purchasedAt: string | null;
}

export interface OrderProduct {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface AddProductRequest {
  productId: number;
  quantity: number;
}

export interface Payment {
  id: number;
  orderId: number;
  paymentMethod: string;
  transactionCode: string | null;
  amount: number;
  status: PaymentStatus;
  paidAt: string | null;
}

// approved simula el resultado de la pasarela de pago (ver nota en el DTO del backend);
// cuando se integre una pasarela real esto lo va a determinar la respuesta de esa pasarela.
export interface RegisterPaymentRequest {
  paymentMethod: string;
  transactionCode?: string;
  approved: boolean;
}
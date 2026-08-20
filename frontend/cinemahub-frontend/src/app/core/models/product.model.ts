// Espejo de com.cinemahub.cinemahub.product.dto.* (backend)

export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK';
export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT';

export interface ProductCategory {
  id: number;
  name: string;
}

export interface ProductCategoryRequest {
  name: string;
}

export interface Product {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string | null;
  imageUrl: string | null;
  price: number;
  stock: number;
  status: ProductStatus;
}

// Coincide con ProductService.create(categoryId, name, price) del backend:
// description no se puede setear en la creación todavía.
export interface CreateProductRequest {
  categoryId: number;
  name: string;
  price: number;
  imageUrl?: string;
}

export interface AdjustStockRequest {
  movementType: MovementType;
  quantity: number;
}

export interface InventoryMovement {
  id: number;
  productId: number;
  movementType: MovementType;
  quantity: number;
  createdAt: string;
}
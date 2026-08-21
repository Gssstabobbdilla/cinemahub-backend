import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Product, ProductCategory } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';
import { ProductCategoryService } from '../../core/services/productCategory.service';

@Component({
  selector: 'app-productos-page',
  standalone: true,
  templateUrl: './productos-page.component.html',
  styleUrl: './productos-page.component.scss'
})
export class ProductosPageComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(ProductCategoryService);

  products = signal<Product[]>([]);
  categories = signal<ProductCategory[]>([]);

  loading = signal(true);
  error = signal<string | null>(null);

  showForm = signal(false);
  editingProduct = signal<Product | null>(null);
  saving = signal(false);

  formName = signal('');
  formPrice = signal<number | null>(null);
  formCategoryId = signal<number | null>(null);
  formImageUrl = signal('');
  formDescription = signal('');

  ngOnInit(): void {
    this.categoryService.findAll().subscribe({
      next: categories => this.categories.set(categories)
    });
    this.loadProducts();
  }

  private loadProducts(): void {
    this.loading.set(true);
    // El backend no expone un findAll() sin filtros para productos; ACTIVE cubre
    // el caso principal de administración diaria.
    this.productService.search({ status: 'ACTIVE' }).subscribe({
      next: products => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  categoryName(categoryId: number): string {
    return this.categories().find(c => c.id === categoryId)?.name ?? '—';
  }

  openCreateForm(): void {
    this.editingProduct.set(null);
    this.formName.set('');
    this.formPrice.set(null);
    this.formCategoryId.set(this.categories()[0]?.id ?? null);
    this.formImageUrl.set('');
    this.formDescription.set('');
    this.showForm.set(true);
  }

  openEditForm(product: Product): void {
    this.editingProduct.set(product);
    this.formName.set(product.name);
    this.formPrice.set(product.price);
    this.formCategoryId.set(product.categoryId);
    this.formImageUrl.set(product.imageUrl ?? '');
    this.formDescription.set(product.description ?? '');
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.error.set(null);
  }

  onNameChange(event: Event): void {
    this.formName.set((event.target as HTMLInputElement).value);
  }

  onPriceChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.formPrice.set(Number.isNaN(value) ? null : value);
  }

  onCategoryChange(event: Event): void {
    this.formCategoryId.set(Number((event.target as HTMLSelectElement).value));
  }

  onImageUrlChange(event: Event): void {
    this.formImageUrl.set((event.target as HTMLInputElement).value);
  }

  onDescriptionChange(event: Event): void {
    this.formDescription.set((event.target as HTMLTextAreaElement).value);
  }

  save(): void {
    const name = this.formName().trim();
    const price = this.formPrice();
    const categoryId = this.formCategoryId();

    if (!name || price === null || !categoryId) {
      this.error.set('Completa nombre, precio y categoría.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const editing = this.editingProduct();

    if (editing) {
      this.productService
        .update(editing.id, {
          name,
          price,
          imageUrl: this.formImageUrl() || undefined,
          description: this.formDescription() || undefined
        })
        .subscribe({
          next: () => {
            this.saving.set(false);
            this.showForm.set(false);
            this.loadProducts();
          },
          error: (err: AppError) => {
            this.error.set(err.message);
            this.saving.set(false);
          }
        });
      return;
    }

    this.productService.create({ categoryId, name, price }).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.loadProducts();
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.saving.set(false);
      }
    });
  }
}
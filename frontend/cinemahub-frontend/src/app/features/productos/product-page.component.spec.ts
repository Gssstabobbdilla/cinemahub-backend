import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Product, ProductCategory } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';
import { ProductCategoryService } from '../../core/services/productCategory.service';

import { ProductosPageComponent } from './productos-page.component';

describe('ProductosPageComponent', () => {
  let fixture: ComponentFixture<ProductosPageComponent>;

  let productServiceSpy: {
    search: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
  };

  let categoryServiceSpy: {
    findAll: ReturnType<typeof vi.fn>;
  };

  const categories: ProductCategory[] = [
    { id: 1, name: 'Snacks' },
    { id: 2, name: 'Bebidas' }
  ];

  const product: Product = {
    id: 5,
    categoryId: 1,
    categoryName: 'Snacks',
    name: 'Nachos',
    description: null,
    price: 12,
    stock: 10,
    imageUrl: null,
    status: 'ACTIVE'
  };

  beforeEach(() => {
    productServiceSpy = {
      search: vi.fn(),
      create: vi.fn(),
      update: vi.fn()
    };

    categoryServiceSpy = {
      findAll: vi.fn()
    };

    categoryServiceSpy.findAll.mockReturnValue(of(categories));
    productServiceSpy.search.mockReturnValue(of([product]));

    TestBed.configureTestingModule({
      imports: [ProductosPageComponent],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: ProductCategoryService, useValue: categoryServiceSpy }
      ]
    });

    fixture = TestBed.createComponent(ProductosPageComponent);
  });

  it('carga categorías y productos ACTIVE al iniciar', () => {
    fixture.detectChanges();

    expect(categoryServiceSpy.findAll).toHaveBeenCalled();
    expect(productServiceSpy.search).toHaveBeenCalledWith({ status: 'ACTIVE' });
    expect(fixture.componentInstance.products()).toEqual([product]);
    expect(fixture.componentInstance.categories()).toEqual(categories);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('setea error() cuando falla la carga de productos', () => {
    productServiceSpy.search.mockReturnValue(
      throwError(() => ({ status: 500, message: 'Error de conexión' }))
    );

    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Error de conexión');
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('categoryName devuelve el nombre correcto según categoryId', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.categoryName(1)).toBe('Snacks');
    expect(fixture.componentInstance.categoryName(2)).toBe('Bebidas');
    expect(fixture.componentInstance.categoryName(99)).toBe('—');
  });

  it('openCreateForm limpia el formulario y precarga la primera categoría', () => {
    fixture.detectChanges();

    fixture.componentInstance.openCreateForm();

    expect(fixture.componentInstance.showForm()).toBe(true);
    expect(fixture.componentInstance.editingProduct()).toBeNull();
    expect(fixture.componentInstance.formName()).toBe('');
    expect(fixture.componentInstance.formCategoryId()).toBe(1);
  });

  it('openEditForm precarga el formulario con los datos del producto', () => {
    fixture.detectChanges();

    fixture.componentInstance.openEditForm(product);

    expect(fixture.componentInstance.showForm()).toBe(true);
    expect(fixture.componentInstance.editingProduct()).toEqual(product);
    expect(fixture.componentInstance.formName()).toBe('Nachos');
    expect(fixture.componentInstance.formPrice()).toBe(12);
    expect(fixture.componentInstance.formCategoryId()).toBe(1);
  });

  it('save valida que nombre, precio y categoría estén completos', () => {
    fixture.detectChanges();

    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formName.set('');

    fixture.componentInstance.save();

    expect(fixture.componentInstance.error()).toBe('Completa nombre, precio y categoría.');
    expect(productServiceSpy.create).not.toHaveBeenCalled();
  });

  it('save crea un producto nuevo cuando no hay editingProduct', () => {
    productServiceSpy.create.mockReturnValue(of(product));

    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formName.set('Nachos');
    fixture.componentInstance.formPrice.set(12);
    fixture.componentInstance.formCategoryId.set(1);

    fixture.componentInstance.save();

    expect(productServiceSpy.create).toHaveBeenCalledWith({
      categoryId: 1,
      name: 'Nachos',
      price: 12
    });
    expect(fixture.componentInstance.showForm()).toBe(false);
    // loadProducts se vuelve a llamar tras guardar
    expect(productServiceSpy.search).toHaveBeenCalledTimes(2);
  });

  it('save actualiza el producto existente cuando hay editingProduct', () => {
    const updated: Product = { ...product, name: 'Nachos Grande', price: 15 };
    productServiceSpy.update.mockReturnValue(of(updated));

    fixture.detectChanges();
    fixture.componentInstance.openEditForm(product);
    fixture.componentInstance.formName.set('Nachos Grande');
    fixture.componentInstance.formPrice.set(15);
    fixture.componentInstance.formImageUrl.set('https://x.com/img.png');
    fixture.componentInstance.formDescription.set('Con queso extra');

    fixture.componentInstance.save();

    expect(productServiceSpy.update).toHaveBeenCalledWith(5, {
      name: 'Nachos Grande',
      price: 15,
      imageUrl: 'https://x.com/img.png',
      description: 'Con queso extra'
    });
    expect(productServiceSpy.create).not.toHaveBeenCalled();
    expect(fixture.componentInstance.showForm()).toBe(false);
  });

  it('save setea error() cuando falla la petición', () => {
    productServiceSpy.create.mockReturnValue(
      throwError(() => ({ status: 400, message: 'Datos inválidos' }))
    );

    fixture.detectChanges();
    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.formName.set('X');
    fixture.componentInstance.formPrice.set(1);
    fixture.componentInstance.formCategoryId.set(1);

    fixture.componentInstance.save();

    expect(fixture.componentInstance.error()).toBe('Datos inválidos');
    expect(fixture.componentInstance.saving()).toBe(false);
    expect(fixture.componentInstance.showForm()).toBe(true);
  });

  it('closeForm oculta el modal y limpia el error', () => {
    fixture.detectChanges();

    fixture.componentInstance.openCreateForm();
    fixture.componentInstance.error.set('algo');

    fixture.componentInstance.closeForm();

    expect(fixture.componentInstance.showForm()).toBe(false);
    expect(fixture.componentInstance.error()).toBeNull();
  });
});
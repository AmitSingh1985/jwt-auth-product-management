
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { environment } from '../../environments/environment';
import { Product } from '../models/product.model';
import { ProductService } from '../core/services/product.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css',
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];

  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  showForm = false;
  editingProduct = false;

  product: Product = this.emptyProduct();

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Sorting
  sortBy = 'id';
  direction: 'asc' | 'desc' = 'asc';

  // Search
  search = '';

  private readonly apiUrl = `${environment.apiUrl}/products`;

  constructor(
    private http: HttpClient,
    private router: Router,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  private emptyProduct(): Product {
    return {
      name: '',
      price: 0,
      quantity: 0,
    };
  }

  // Load products from the backend
  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.productService
      .getAllProducts(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.direction,
        this.search
      )
      .subscribe({
        next: (response) => {
          const page = response.data;

          this.products = page?.content ?? [];
          this.totalElements = page?.totalElements ?? 0;
          this.totalPages = page?.totalPages ?? 0;
          this.currentPage = page?.number ?? 0;
          this.pageSize = page?.size ?? this.pageSize;

          this.loading = false;
        },
        error: (error) => {
          this.loading = false;

          if (error.status === 401) {
            this.router.navigate(['/login']);
          } else {
            this.errorMessage =
              error.error?.message ?? 'Unable to load products.';
          }
        },
      });
  }

  // Search by product name
  onSearch(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  // Clear search
  clearSearch(): void {
    this.search = '';
    this.currentPage = 0;
    this.loadProducts();
  }

  // Sort by column
  onSort(field: string): void {
    if (this.sortBy === field) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.direction = 'asc';
    }

    this.currentPage = 0;
    this.loadProducts();
  }

  // Change page size
  onPageSizeChange(value: string | number): void {
    const newSize = Number(value);

    if (![5, 10, 25, 50].includes(newSize)) {
      return;
    }

    this.pageSize = newSize;
    this.currentPage = 0;
    this.loadProducts();
  }

  // Go to a particular page (page numbers are zero-based)
  goToPage(page: number): void {
    if (
      page < 0 ||
      page >= this.totalPages ||
      page === this.currentPage
    ) {
      return;
    }

    this.currentPage = page;
    this.loadProducts();
  }

  get pages(): number[] {
    return Array.from(
      { length: this.totalPages },
      (_, index) => index
    );
  }

  // Open the add form
  openAddForm(): void {
    this.product = this.emptyProduct();
    this.editingProduct = false;
    this.showForm = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Open the edit form
  editProduct(product: Product): void {
    this.product = { ...product };
    this.editingProduct = true;
    this.showForm = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Save a new or existing product
  saveProduct(): void {
    this.errorMessage = '';
    this.successMessage = '';

    const name = this.product.name.trim();

    if (!name) {
      this.errorMessage = 'Product name is required.';
      return;
    }

    if (
      !Number.isFinite(Number(this.product.price)) ||
      Number(this.product.price) < 0
    ) {
      this.errorMessage = 'Enter a valid, non-negative price.';
      return;
    }

    if (
      !Number.isInteger(Number(this.product.quantity)) ||
      Number(this.product.quantity) < 0
    ) {
      this.errorMessage =
        'Quantity must be a non-negative whole number.';
      return;
    }

    const payload: Product = {
      ...this.product,
      name,
      price: Number(this.product.price),
      quantity: Number(this.product.quantity),
    };

    this.saving = true;

    const request =
      this.editingProduct && payload.id != null
        ? this.http.put(
            `${this.apiUrl}/${payload.id}`,
            payload
          )
        : this.http.post(this.apiUrl, payload);

    request.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.successMessage = this.editingProduct
          ? 'Product updated successfully.'
          : 'Product added successfully.';

        // Return to first page after adding a product
        if (!this.editingProduct) {
          this.currentPage = 0;
        }

        this.loadProducts();
      },
      error: (error) => {
        this.saving = false;
        this.handleError(error);
      },
    });
  }

  // Delete a product by ID
  deleteProduct(id?: number): void {
    if (id == null) {
      return;
    }

    if (!confirm('Are you sure you want to delete this product?')) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.successMessage = 'Product deleted successfully.';

        // If the current page becomes empty, go back one page
        if (this.products.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }

        this.loadProducts();
      },
      error: (error) => this.handleError(error),
    });
  }

  // Close the form
  cancelForm(): void {
    this.showForm = false;
    this.errorMessage = '';
  }

  // Dashboard navigation
  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  private handleError(error: any): void {
    if (error.status === 401) {
      this.router.navigate(['/login']);
      return;
    }

    this.errorMessage =
      error.error?.message ?? 'The operation failed. Please try again.';
  }
}
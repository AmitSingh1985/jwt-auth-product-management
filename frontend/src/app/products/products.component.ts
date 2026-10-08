import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { environment } from '../../environments/environment';

interface Product {
  id?: number;
  name: string;
  price: number;
  quantity: number;
}

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css',
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];

  loading = true;
  errorMessage = '';
  successMessage = '';

  showForm = false;
  editingProduct = false;

  product: Product = {
    name: '',
    price: 0,
    quantity: 0,
  };

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<any>(`${environment.apiUrl}/products`).subscribe({
      next: (response) => {
        /*
         * Backend returns:
         * {
         *   success: true,
         *   message: "...",
         *   data: [...]
         * }
         */

        this.products = response.data || [];

        this.loading = false;
      },

      error: (error) => {
        console.error('Products error:', error);

        this.loading = false;

        if (error.status === 401) {
          this.router.navigate(['/login']);
        } else {
          this.errorMessage = 'Unable to load products.';
        }
      },
    });
  }

  openAddForm(): void {
    this.editingProduct = false;

    this.product = {
      name: '',
      price: 0,
      quantity: 0,
    };

    this.successMessage = '';
    this.errorMessage = '';

    this.showForm = true;
  }

  editProduct(product: Product): void {
    this.editingProduct = true;

    this.product = {
      id: product.id,
      name: product.name,
      price: product.price,
      quantity: product.quantity,
    };

    this.successMessage = '';
    this.errorMessage = '';

    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;

    this.product = {
      name: '',
      price: 0,
      quantity: 0,
    };

    this.errorMessage = '';
  }

  saveProduct(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.product.name.trim()) {
      this.errorMessage = 'Product name is required.';
      return;
    }

    if (this.product.price < 0) {
      this.errorMessage = 'Price cannot be negative.';
      return;
    }

    if (this.product.quantity < 0) {
      this.errorMessage = 'Quantity cannot be negative.';
      return;
    }

    if (this.editingProduct && this.product.id) {
      this.http
        .put<any>(
          `${environment.apiUrl}/products/${this.product.id}`,
          this.product,
        )
        .subscribe({
          next: () => {
            this.successMessage = 'Product updated successfully.';

            this.showForm = false;

            this.loadProducts();
          },

          error: (error) => {
            console.error('Update product error:', error);

            if (error.status === 401) {
              this.router.navigate(['/login']);
            } else {
              this.errorMessage = 'Unable to update product.';
            }
          },
        });
    } else {
      this.http
        .post<any>(`${environment.apiUrl}/products`, this.product)
        .subscribe({
          next: () => {
            this.successMessage = 'Product added successfully.';

            this.showForm = false;

            this.loadProducts();
          },

          error: (error) => {
            console.error('Add product error:', error);

            if (error.status === 401) {
              this.router.navigate(['/login']);
            } else {
              this.errorMessage = 'Unable to add product.';
            }
          },
        });
    }
  }

  deleteProduct(product: Product): void {
    if (!product.id) {
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete "${product.name}"?`,
    );

    if (!confirmed) {
      return;
    }

    this.http
      .delete<any>(`${environment.apiUrl}/products/${product.id}`)
      .subscribe({
        next: () => {
          this.successMessage = 'Product deleted successfully.';

          this.loadProducts();
        },

        error: (error) => {
          console.error('Delete product error:', error);

          if (error.status === 401) {
            this.router.navigate(['/login']);
          } else {
            this.errorMessage = 'Unable to delete product.';
          }
        },
      });
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}

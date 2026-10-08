import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { environment } from '../../environments/environment';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  message = '';
  loading = true;
  loggingOut = false;
  errorMessage = '';

  username = '';
  role = '';
  isAdmin = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    // Get logged-in user information
    this.username = this.authService.getUsername() || '';
    this.role = this.authService.getRole() || '';

    // Check administrator role
    this.isAdmin = this.role === 'ADMINISTRATOR';

    console.log('Dashboard username:', this.username);
    console.log('Dashboard role:', this.role);
    console.log('Dashboard isAdmin:', this.isAdmin);

    this.loadDashboard();
  }

  loadDashboard(): void {
    this.http
      .get(`${environment.apiUrl}/dashboard`, {
        responseType: 'text',
      })
      .subscribe({
        next: (response: string) => {
          this.message = response;
          this.loading = false;
        },

        error: (error: any) => {
          this.loading = false;

          console.error('Dashboard error:', error);

          if (error.status === 401) {
            this.authService.clearSession();
            this.router.navigate(['/login']);
          } else {
            this.errorMessage = 'Unable to load dashboard.';
          }
        },
      });
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  goToProducts(): void {
    this.router.navigate(['/products']);
  }

  logout(): void {
    this.loggingOut = true;
    this.errorMessage = '';

    this.authService.logout().subscribe({
      next: (response: string) => {
        console.log('Logout response:', response);

        this.authService.clearSession();
        this.router.navigate(['/login']);
      },

      error: (error: any) => {
        console.error('Logout error:', error);

        // Even if backend logout fails,
        // clear the frontend session.
        this.authService.clearSession();
        this.router.navigate(['/login']);
      },
    });
  }
}

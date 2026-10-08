import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { environment } from '../../environments/environment';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css',
})
export class SettingsComponent implements OnInit {
  message = '';
  errorMessage = '';
  loading = true;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router,
  ) {}
  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
  ngOnInit(): void {
    const role = this.authService.getRole();

    if (role !== 'ADMINISTRATOR') {
      this.loading = false;
      this.errorMessage = 'Access Denied. Administrator only.';
      return;
    }

    this.loadSettings();
  }

  loadSettings(): void {
    this.http
      .get(`${environment.apiUrl}/settings`, {
        responseType: 'text',
      })
      .subscribe({
        next: (response: string) => {
          this.message = response;
          this.loading = false;
        },

        error: (error: any) => {
          this.loading = false;

          if (error.status === 401) {
            this.authService.clearSession();
            this.router.navigate(['/login']);
          } else if (error.status === 403) {
            this.errorMessage = 'Access Denied. Administrator only.';
          } else {
            this.errorMessage = 'Unable to load settings.';
          }
        },
      });
  }
}

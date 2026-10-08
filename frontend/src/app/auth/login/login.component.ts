import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  AuthService,
  AuthResponse
} from '../../core/services/auth.service';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  username = '';
  password = '';

  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {

    this.errorMessage = '';

    if (!this.username || !this.password) {
      this.errorMessage = 'Username and password are required';
      return;
    }

    this.loading = true;

    this.authService.login({
      username: this.username,
      password: this.password
    }).subscribe({
      next: (response: AuthResponse) => {

        this.authService.saveToken(response);

        this.loading = false;

        this.router.navigate(['/dashboard']);
      },

      error: (error) => {

        this.loading = false;

        if (error.status === 401) {
          this.errorMessage = 'Login Failed';
        } else {
          this.errorMessage =
            error.error?.message || 'Something went wrong';
        }
      }
    });
  }
}
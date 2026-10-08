import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
export class SignupComponent {
  username = '';
  email = '';
  password = '';
  userType = 'STANDARD_USER';

  otp = '';

  otpSent = false;
  loading = false;

  message = '';
  errorMessage = '';
  testOtp = '';

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  sendOtp(): void {
    this.message = '';
    this.errorMessage = '';
    this.testOtp = '';

    if (!this.username || !this.email || !this.password || !this.userType) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }

    this.loading = true;

    const request = {
      username: this.username,
      email: this.email,
      password: this.password,
      userType: this.userType,
    };

    this.http
      .post<any>(`${environment.apiUrl}/auth/signup/request-otp`, request)
      .subscribe({
        next: (response) => {
          this.loading = false;
          this.otpSent = true;

          this.message = response.message || 'OTP generated successfully';

          // Test mode: backend returns OTP
          if (response.otp) {
            this.testOtp = response.otp;
          }
        },

        error: (error) => {
          this.loading = false;

          console.error('OTP request error:', error);

          if (error.error?.message) {
            this.errorMessage = error.error.message;
          } else {
            this.errorMessage = 'Unable to send OTP.';
          }
        },
      });
  }

  verifyOtp(): void {
    this.message = '';
    this.errorMessage = '';

    if (!this.otp) {
      this.errorMessage = 'Please enter the OTP.';
      return;
    }

    this.loading = true;

    const request = {
      email: this.email,
      otp: this.otp,
    };

    this.http
      .post(`${environment.apiUrl}/auth/signup/verify-otp`, request, {
        responseType: 'text',
      })
      .subscribe({
        next: (response: string) => {
          this.loading = false;

          this.message = response;

          // Registration successful
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1500);
        },

        error: (error) => {
          this.loading = false;

          console.error('OTP verification error:', error);

          if (error.error?.message) {
            this.errorMessage = error.error.message;
          } else if (typeof error.error === 'string') {
            this.errorMessage = error.error;
          } else {
            this.errorMessage = 'Invalid or expired OTP.';
          }
        },
      });
  }

  backToLogin(): void {
    this.router.navigate(['/login']);
  }
}

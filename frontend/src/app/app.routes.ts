import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [

  // Default route
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  // Public - Login
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(
        m => m.LoginComponent
      )
  },

  // Public - Signup
  {
    path: 'signup',
    loadComponent: () =>
      import('./auth/signup/signup.component').then(
        m => m.SignupComponent
      )
  },

  // Protected - Dashboard
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./dashboard/dashboard.component').then(
        m => m.DashboardComponent
      )
  },

  // Protected - Settings
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./settings/settings.component').then(
        m => m.SettingsComponent
      )
  },

  // Protected - Products
  {
    path: 'products',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./products/products.component').then(
        m => m.ProductsComponent
      )
  },

  // Unknown route
  {
    path: '**',
    redirectTo: 'login'
  }
];
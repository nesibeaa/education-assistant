import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  
  {
    path: '',
    loadComponent: () =>
      import('./home/home.component').then((m) => m.HomeComponent),
  },

  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },

  
  {
    path: 'login',
    loadComponent: () =>
      import('./login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./register/register.component').then((m) => m.RegisterComponent),
  },

  
  {
    path: 'history',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./history/history.component').then((m) => m.HistoryComponent),
  },

  
  {
    path: 'chat/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./chat/chat.component').then((m) => m.ChatComponent),
  },

  
  { path: 'chat', redirectTo: 'history', pathMatch: 'full' },

 
  { path: '**', redirectTo: '' },
];
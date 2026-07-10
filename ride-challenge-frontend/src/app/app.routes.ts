import { Routes } from '@angular/router';
import { sessionGuard } from './core/session/session.guard';
import { createRoleGuard } from './core/session/role.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [sessionGuard],
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/default-dashboard/default-dashboard.component').then(
        (m) => m.DefaultDashboardComponent
      ),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'client',
    canActivate: [sessionGuard, createRoleGuard('CLIENT')],
    children: [
      {
        path: 'rides',
        loadComponent: () =>
          import('./features/client-rides/client-rides.component').then(
            (m) => m.ClientRidesComponent
          ),
      },
    ],
  },
  {
    path: 'driver',
    canActivate: [sessionGuard, createRoleGuard('DRIVER')],
    children: [
      {
        path: 'rides/available',
        loadComponent: () =>
          import('./features/driver-available-rides/driver-available-rides.component').then(
            (m) => m.DriverAvailableRidesComponent
          ),
      },
    ],
  },
  {
    path: 'forbidden',
    loadComponent: () =>
      import('./features/forbidden/forbidden.component').then(
        (m) => m.ForbiddenComponent
      ),
  },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];